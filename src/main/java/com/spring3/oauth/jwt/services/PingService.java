package com.spring3.oauth.jwt.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PingService {

    @Value("${PING_API}")
    private String pingApiUrl;

    private final RestTemplate restTemplate;

    public PingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
    public void pingApi() {
        try {
            String response = restTemplate.getForObject(pingApiUrl, String.class);
            log.info("Ping API response: {}", response);
        } catch (Exception e) {
            log.error("Error when pinging API: {}", e.getMessage());
        }
    }
} 