package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.RefreshToken;
import com.spring3.oauth.jwt.repositories.RefreshTokenRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    // Phương thức này sẽ tạo refresh token mới, và xóa token cũ nếu có.
    public RefreshToken createRefreshToken(String username) {
        // Tìm xem có refresh token nào đã tồn tại cho user không
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserUsername(username);

        // Nếu có token, thì xóa token cũ
        if (existingToken.isPresent()) {
            refreshTokenRepository.delete(existingToken.get());
        }

        // Tạo token mới
        RefreshToken refreshToken = RefreshToken.builder()
            .user(userRepository.findByUsername(username))
            .token(UUID.randomUUID().toString())
            .expiryDate(Instant.now().plusMillis(1000 * 60 * 10)) // 10 phút
            .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // Tìm refresh token bằng token
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Kiểm tra và xác thực thời hạn của token
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }
}
