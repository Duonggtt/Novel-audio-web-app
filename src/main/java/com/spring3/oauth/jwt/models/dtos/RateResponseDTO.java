package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateResponseDTO {
    private Integer id;
    private int rateQuantity;
    private BigDecimal rate;
    private Integer novelId;
}
