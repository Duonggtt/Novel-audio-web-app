package com.spring3.oauth.jwt.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentCoinRequest {
    private long amount;
    private String orderInfo;
    private String coinPackageId;
}