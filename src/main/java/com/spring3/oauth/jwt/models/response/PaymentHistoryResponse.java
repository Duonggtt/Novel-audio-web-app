package com.spring3.oauth.jwt.models.response;

import lombok.Data;

@Data
public class PaymentHistoryResponse {
    private String transactionNo;
    private long amount;
    private String orderInfo;
    private String payDate;
    private String responseStatus;
}
