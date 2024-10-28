package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.RateResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;
import com.spring3.oauth.jwt.models.request.UpsertRateRequest;

public interface RateService {
    RateResponseDTO createRatePoint(UpsertRateRequest request);
    void deleteRatePoint(Integer id);
    RateResponseDTO updateRatePoint(Integer id, UpdateRatePointRequest request);
    RateResponseDTO getRatePointByNovelId(Integer novelId);
}
