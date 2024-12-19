package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateListResponseDTO {
    private Integer id;
    private String fullName;
    private String novelTitle;
    private LocalDateTime createdAt;
    private BigDecimal userRate;
}
