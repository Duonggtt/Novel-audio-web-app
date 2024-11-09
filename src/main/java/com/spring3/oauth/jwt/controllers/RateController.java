package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;
import com.spring3.oauth.jwt.services.impl.RateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/rates")
public class RateController {

    private final RateServiceImpl rateService;

    @GetMapping("/in-novel/{slug}")
    public ResponseEntity<?> getRateByNovelSlug(@PathVariable String slug, @RequestParam long userId) {
        return ResponseEntity.ok(rateService.getRatePointByNovelSlug(slug, userId));
    }

    @PutMapping("/set-rate/{slug}")
    public ResponseEntity<?> updateRate(@PathVariable String slug,
                                        @RequestBody UpdateRatePointRequest request,
                                        @RequestParam long userId) {
        return ResponseEntity.ok(rateService.updateRatePoint(slug, request, userId));
    }

}
