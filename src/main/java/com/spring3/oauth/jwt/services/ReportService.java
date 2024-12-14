package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.UserActivityReport;
import com.spring3.oauth.jwt.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    public List<List<UserActivityReport>> getReportsWithAllHours(String date) {
        // Lấy báo cáo hiện có cho cả 2 api (comments và read-chapter)
        List<UserActivityReport> commentReports = reportRepository.findByDateAndApiPath(date, "comments");
        List<UserActivityReport> readChapterReports = reportRepository.findByDateAndApiPath(date, "read-chapter");

        // Điền các giờ còn thiếu cho cả 2 api
        List<UserActivityReport> filledCommentReports = fillMissingHours(commentReports, date, "comments");
        List<UserActivityReport> filledReadChapterReports = fillMissingHours(readChapterReports, date, "read-chapter");

        // Trả về một danh sách chứa 2 danh sách con: 1 cho comments và 1 cho read-chapter
        return List.of(filledCommentReports, filledReadChapterReports);
    }

    private List<UserActivityReport> fillMissingHours(List<UserActivityReport> existingReports, String date, String apiPath) {
        // Tạo danh sách 24 giờ với count mặc định là 0
        return IntStream.range(0, 24)
            .mapToObj(hour -> {
                String hourStr = String.format("%02d", hour);
                return existingReports.stream()
                    .filter(r -> r.getApiPath().equals(apiPath) && r.getHour().equals(hourStr))
                    .findFirst()
                    .orElse(UserActivityReport.builder()
                        .date(date)
                        .apiPath(apiPath)
                        .hour(hourStr)
                        .count(0)
                        .build());
            })
            .collect(Collectors.toList());
    }
}
