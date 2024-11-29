package com.spring3.oauth.jwt.models.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String transactionNo;
    private Long userId;
    private String packageName;
    private LocalDateTime expirationDate;
}