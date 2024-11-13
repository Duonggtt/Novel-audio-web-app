package com.spring3.oauth.jwt.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponseDTO {
    private String accessToken;
    private String token;
    private long userId;
    private String role;
}
