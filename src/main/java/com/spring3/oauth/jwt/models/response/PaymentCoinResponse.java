package com.spring3.oauth.jwt.models.response;

import lombok.Data;

@Data
public class PaymentCoinResponse {
    private boolean success;
    private String message;
    private String transactionNo;
    private String packageName;
    private String finalCoinAmount;
    private int discount;
}
