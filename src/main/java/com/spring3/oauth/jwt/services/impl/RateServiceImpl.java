package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.Rate;
import com.spring3.oauth.jwt.entity.UserRate;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.RateResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateRatePointRequest;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.repositories.RateRepository;
import com.spring3.oauth.jwt.repositories.UserRateRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.RateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepository;
    private final NovelRepository novelRepository;
    private final UserRateRepository userRateRepository;
    private final UserRepository userRepository;

    @Override
    public RateResponseDTO updateRatePoint(String slug, UpdateRatePointRequest request, long userId) {

        Novel novel = novelRepository.findBySlug(slug);
        if (novel == null) {
            throw new NotFoundException("Novel not found");
        }

        // Kiểm tra và cập nhật rate của user
        UserRate userRate = userRateRepository.findByUser_IdAndNovel_Slug(userId, slug)
            .orElse(new UserRate());

        if (userRate.getId() != null) {
            throw new IllegalStateException("User has already rated this novel");
        }

        // Cập nhật thông tin rate
        userRate.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found")));
        userRate.setNovel(novel);
        userRate.setRatePoint(request.getRatePoint());
        userRate.setRatedAt(LocalDateTime.now());
        userRateRepository.save(userRate);

        Rate rate = rateRepository.findByNovel_Slug(slug);

        if(rate == null) {
            rate = new Rate();
            rate.setNovel(novel);
            rate.setRateQuantity(0);
            rate.setRate(BigDecimal.ZERO);
        }

        // Tăng số lượng đánh giá trước
        rate.setRateQuantity(rate.getRateQuantity() + 1);

        // Tính toán tổng điểm hiện tại
        BigDecimal currentTotalRate = rate.getRate().multiply(new BigDecimal(rate.getRateQuantity() - 1)); // Tổng hiện tại
        BigDecimal newTotalRate = currentTotalRate.add(request.getRatePoint()); // Tổng mới sau khi thêm rate mới

        // Tính trung bình mới
        BigDecimal newAverageRate = newTotalRate.divide(new BigDecimal(rate.getRateQuantity()), 2, RoundingMode.HALF_UP);

        // Cập nhật lại giá trị rate
        rate.setRate(newAverageRate);
        novel.setAverageRatings(newAverageRate);

        // Lưu rate sau khi cập nhật
        rateRepository.save(rate);

        return RateResponseDTO.builder()
            .id(rate.getId())
            .rateQuantity(rate.getRateQuantity())
            .rate(rate.getRate())
            .slug(rate.getNovel().getSlug())
            .userRate(userRate.getRatePoint())
            .build();
    }


    @Override
    public RateResponseDTO getRatePointByNovelSlug(String slug, long userId) {
        Rate rate = rateRepository.findByNovel_Slug(slug);
        if (rate == null) {
            throw new NotFoundException("Rate not found for novel");
        }

        BigDecimal userRatePoint = userRateRepository.findByUser_IdAndNovel_Slug(userId, slug)
            .map(UserRate::getRatePoint)
            .orElse(null);

        return RateResponseDTO.builder()
            .id(rate.getId())
            .rateQuantity(rate.getRateQuantity())
            .rate(rate.getRate())
            .slug(rate.getNovel().getSlug())
            .userRate(userRatePoint)
            .build();
    }
}
