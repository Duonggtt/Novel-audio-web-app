package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.Report;
import com.spring3.oauth.jwt.repositories.ReportRepository;
import com.spring3.oauth.jwt.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/yesterday")
    public List<Report> getReports(@RequestParam(required = false) String apiPath) {

        // Lấy ngày hôm qua
        String yesterday = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return reportService.getReportsWithAllHours(yesterday, apiPath);
    }
}
