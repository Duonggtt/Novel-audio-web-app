package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;
import com.spring3.oauth.jwt.models.request.UpsertRateRequest;
import com.spring3.oauth.jwt.services.impl.RateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/rates")
public class RateController {

    private final RateServiceImpl rateService;

    @GetMapping("/in-novel/{novelId}")
    public ResponseEntity<?> getRateByNovelId(@PathVariable Integer novelId) {
        return ResponseEntity.ok(rateService.getRatePointByNovelId(novelId));
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteRate(@PathVariable Integer id) {
        rateService.deleteRatePoint(id);
        return ResponseEntity.ok("Delete rate success");
    }

    @PutMapping("/set-rate/{id}")
    public ResponseEntity<?> updateRate(@PathVariable Integer id, @RequestBody UpdateRatePointRequest request) {
        return ResponseEntity.ok(rateService.updateRatePoint(id, request));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRate(@RequestBody UpsertRateRequest request) {
        return ResponseEntity.ok(rateService.createRatePoint(request));
    }

}
