package com.spring3.oauth.jwt.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SubscriptionRequest {
    private Long packageId;
    private Long userId;
    private Long amount;
    private String orderInfo;
}
