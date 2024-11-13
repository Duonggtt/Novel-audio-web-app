package com.spring3.oauth.jwt.models.response;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
}