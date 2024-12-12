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

    public List<UserActivityReport> getReportsWithAllHours(String date, String apiPath) {
        // Lấy báo cáo hiện có
        List<UserActivityReport> existingReports = apiPath != null
            ? reportRepository.findByDateAndApiPath(date, apiPath)
            : reportRepository.findByDate(date);

        // Điền các giờ còn thiếu
        return fillMissingHours(existingReports, date, apiPath);
    }

    private List<UserActivityReport> fillMissingHours(List<UserActivityReport> existingUserActivityReports, String date, String apiPath) {
        // Tạo danh sách 24 giờ với count mặc định là 0
        return IntStream.range(0, 24)
            .mapToObj(hour -> {
                String hourStr = String.format("%02d", hour);
                return existingUserActivityReports.stream()
                    .filter(r -> r.getHour().equals(hourStr))
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
