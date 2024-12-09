package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.services.impl.NovelServiceImpl;
import com.spring3.oauth.jwt.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final UserServiceImpl userService;
    private final NovelServiceImpl novelService;

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
}
