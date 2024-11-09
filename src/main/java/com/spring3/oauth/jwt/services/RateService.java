package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.RateResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;

import java.math.BigDecimal;

public interface RateService {
    RateResponseDTO updateRatePoint(String slug, UpdateRatePointRequest request, long userId);
    RateResponseDTO getRatePointByNovelSlug(String slug, long userId);
}
