package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.dtos.TotalQuantityResponseDTO;
import com.spring3.oauth.jwt.services.ReportService;
import com.spring3.oauth.jwt.services.impl.NovelServiceImpl;
import com.spring3.oauth.jwt.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final UserServiceImpl userService;
    private final NovelServiceImpl novelService;
    private final ReportService reportService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/user-tiers")
    public ResponseEntity<?> getUserTiersEachRanges() {
        return ResponseEntity.ok(userService.getAllUsersQuantityForEachLevel());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/user-points")
    public ResponseEntity<?> getUserPointsEachRanges() {
        return ResponseEntity.ok(userService.getUserCountByScoreRange());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/novel-genres")
    public ResponseEntity<?> getNovelGenresEachRanges() {
        return ResponseEntity.ok(novelService.getNovelCountByGenre());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/novel-status")
    public ResponseEntity<?> getNovelStatusForWeek() {
        return ResponseEntity.ok(null);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/likes")
    public ResponseEntity<?> getNovelLikesForWeek() {
        return ResponseEntity.ok(userService.getTotalLikeCountsForLastWeek());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/amounts")
    public ResponseEntity<?> getAmountReportsPerWeek() {
        return ResponseEntity.ok(reportService.getPayReport());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/quantity-info")
    public ResponseEntity<?> getAllQuantityInfo() {
        List<TotalQuantityResponseDTO> metrics = reportService.getAllQuantityInfo();
        Map<String, TotalQuantityResponseDTO> response = new HashMap<>();

        // Convert list to map for easier client-side handling
        metrics.forEach(metric -> response.put(metric.getName(), metric));

        return ResponseEntity.ok(response);
    }

}
