package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.TopReadResponseDTO;
import com.spring3.oauth.jwt.models.dtos.TopScoreResponseDTO;
import com.spring3.oauth.jwt.models.dtos.UserResponseDTO;
import com.spring3.oauth.jwt.models.request.ForgotPassRequest;
import com.spring3.oauth.jwt.models.request.GenresSelectedRequest;
import com.spring3.oauth.jwt.models.request.UpdateUserRequest;
import com.spring3.oauth.jwt.models.request.UserRequest;
import com.spring3.oauth.jwt.models.response.UserResponse;

import java.util.List;


public interface UserService {

    UserResponse saveUser(UserRequest userRequest);

    UserResponse getUser();

    UserResponseDTO getProfile(String username);

    List<UserResponse> getAllUser();

    boolean isAdmin(String username);
    void forgotPass(ForgotPassRequest request);
    UserResponse resetPassword(String email, String newPassword);
    String verifyOtp(String email, String otpCode);
    void updateReadCountChapter(long userId);
    String confirmPaymentStatus(String username);
    UserResponseDTO updateProfile(UpdateUserRequest request, String username);
    UserResponseDTO updateSelectedGenres(Long userId, GenresSelectedRequest request);
    List<TopReadResponseDTO> getTopRead();
    List<TopScoreResponseDTO> getTopPoint();
    long getUserIdByUsername(String username);
    String getRole(String username);
}
