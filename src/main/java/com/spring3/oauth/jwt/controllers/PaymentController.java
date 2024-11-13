package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.PaymentRequest;
import com.spring3.oauth.jwt.models.response.PaymentResponse;
import com.spring3.oauth.jwt.services.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody PaymentRequest request) {
        String paymentUrl = vnPayService.createPayment(request);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<PaymentResponse> paymentCallback(@RequestParam Map<String, String> queryParams) {
        PaymentResponse response = vnPayService.processPaymentResponse(queryParams);
        return ResponseEntity.ok(response);
    }
}