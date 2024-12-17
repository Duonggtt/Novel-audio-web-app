package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalQuantityResponseDTO {
    private long novelQuantity;
    private long chapterQuantity;
    private long userQuantity;
    private long readQuantity;
    private long commentQuantity;
    private long totalAmount;
}
