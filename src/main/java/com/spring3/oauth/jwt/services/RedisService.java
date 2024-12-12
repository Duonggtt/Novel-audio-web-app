package com.spring3.oauth.jwt.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
@Slf4j
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void incrementApiCall(String apiPath) {
        String currentHour = getCurrentHour();
        String counterKey = "api:" + apiPath + "-h:" + currentHour;

        // Sử dụng increment để tăng giá trị bộ đếm
        Long newCount = redisTemplate.opsForValue().increment(counterKey, 1);

        log.info("Incremented API call for path '{}' at hour '{}' with count '{}'",
            apiPath, currentHour, newCount);
    }

    // Lấy khung giờ hiện tại dưới dạng "HH"
    private String getCurrentHour() {
        return DateTimeFormatter.ofPattern("HH").format(LocalDateTime.now());
    }

    // Tạo key Redis theo định dạng: api:{path}:{hour}
    private String buildRedisKey(String apiPath, String hour) {
        return "api:" + apiPath + "-h:" + hour; // Ví dụ: "api:/api/v1/novels/like-h:17"
    }
}
