package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.Rate;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.RateResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;
import com.spring3.oauth.jwt.models.request.UpsertRateRequest;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.repositories.RateRepository;
import com.spring3.oauth.jwt.services.RateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepository;
    private final NovelRepository novelRepository;

    @Override
    public RateResponseDTO createRatePoint(UpsertRateRequest request) {

        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new NotFoundException("Novel not found"));

        Rate rate = new Rate();
        rate.setRate(request.getRate());
        rate.setRateQuantity(0);
        rate.setNovel(novel);
        rateRepository.save(rate);
        return convertToDto(rate);
    }

    @Override
    public void deleteRatePoint(Integer id) {
        Rate rate = rateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Rate not found"));
        rateRepository.delete(rate);
    }

    @Override
    public RateResponseDTO updateRatePoint(Integer id, UpdateRatePointRequest request) {

        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new NotFoundException("Novel not found"));

        Rate rate = rateRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Rate not found"));

        // Tăng số lượng đánh giá trước
        rate.setRateQuantity(rate.getRateQuantity() + 1);

        // Tính toán tổng điểm hiện tại
        BigDecimal currentTotalRate = rate.getRate().multiply(new BigDecimal(rate.getRateQuantity() - 1)); // Tổng hiện tại
        BigDecimal newTotalRate = currentTotalRate.add(request.getRate()); // Tổng mới sau khi thêm rate mới

        // Tính trung bình mới
        BigDecimal newAverageRate = newTotalRate.divide(new BigDecimal(rate.getRateQuantity()), 2, RoundingMode.HALF_UP);

        // Cập nhật lại giá trị rate
        rate.setRate(newAverageRate);
        novel.setAverageRatings(newAverageRate);

        // Lưu rate sau khi cập nhật
        rateRepository.save(rate);

        return convertToDto(rate);
    }


    @Override
    public RateResponseDTO getRatePointByNovelId(Integer novelId) {
        Rate rate = rateRepository.findByNovelId(novelId);
        return convertToDto(rate);
    }

    RateResponseDTO convertToDto(Rate rate) {
        return RateResponseDTO.builder()
            .id(rate.getId())
            .rateQuantity(rate.getRateQuantity())
            .rate(rate.getRate())
            .novelId(rate.getNovel().getId())
            .build();
    }
}
