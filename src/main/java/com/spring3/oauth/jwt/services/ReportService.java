package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Report;
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

    public List<Report> getReportsWithAllHours(String date, String apiPath) {
        // Lấy báo cáo hiện có
        List<Report> existingReports = apiPath != null
            ? reportRepository.findByDateAndApiPath(date, apiPath)
            : reportRepository.findByDate(date);

        // Điền các giờ còn thiếu
        return fillMissingHours(existingReports, date, apiPath);
    }

    private List<Report> fillMissingHours(List<Report> existingReports, String date, String apiPath) {
        // Tạo danh sách 24 giờ với count mặc định là 0
        return IntStream.range(0, 24)
            .mapToObj(hour -> {
                String hourStr = String.format("%02d", hour);
                return existingReports.stream()
                    .filter(r -> r.getHour().equals(hourStr))
                    .findFirst()
                    .orElse(Report.builder()
                        .date(date)
                        .apiPath(apiPath)
                        .hour(hourStr)
                        .count(0)
                        .build());
            })
            .collect(Collectors.toList());
    }
}
