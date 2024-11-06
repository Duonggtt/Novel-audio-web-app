package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopScoreResponseDTO {
    private long userId;
    private String fullName;
    private String imagePath;
    private String tierName;
    private int point;
}
