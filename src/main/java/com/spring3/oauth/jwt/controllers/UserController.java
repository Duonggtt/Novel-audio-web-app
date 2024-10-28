package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.RefreshToken;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.dtos.*;
import com.spring3.oauth.jwt.models.request.*;
import com.spring3.oauth.jwt.models.response.UserResponse;
import com.spring3.oauth.jwt.services.JwtService;
import com.spring3.oauth.jwt.services.RefreshTokenService;
import com.spring3.oauth.jwt.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPassRequest forgotPassRequest) {
        try {
            userService.forgotPass(forgotPassRequest);
            return ResponseEntity.ok("OTP has been sent to your email.");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            UserResponse userResponse = userService.resetPassword(resetPasswordRequest.getEmail(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        try {
            String result = userService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtpCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-read-count-chapter")
    public ResponseEntity<?> updateReadCountChapter(@RequestParam long userId) {
        userService.updateReadCountChapter(userId);
        return ResponseEntity.ok("Update read count chapter successfully.");
    }

    @PostMapping(value = "/save")
    public ResponseEntity<?> saveUser(@RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = userService.saveUser(userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/set-account-status-active")
    public ResponseEntity<?> setAccountStatusActive(@RequestParam String username) {
        return ResponseEntity.ok(userService.confirmPaymentStatus(username));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> userResponses = userService.getAllUser();
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/detail")
    public ResponseEntity<UserResponse> getUserDetail() {
        try {
            UserResponse userResponse = userService.getUser();
            return ResponseEntity.ok().body(userResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserResponseDTO> getProfile(@PathVariable String username) {
        UserResponseDTO userResponseDTO = userService.getProfile(username);
        return ResponseEntity.ok().body(userResponseDTO);
    }

    @PutMapping("/profile/update-profile")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody UpdateUserRequest request, @RequestParam String username) {
        UserResponseDTO userResponseDTO = userService.updateProfile(request, username);
        return ResponseEntity.ok().body(userResponseDTO);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/test")
    public String test() {
        try {
            return "Welcome";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public JwtResponseDTO AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            boolean isAdmin = userService.isAdmin(authRequestDTO.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return JwtResponseDTO.builder()
                .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                .token(refreshToken.getToken())
                .isAdmin(isAdmin)
                .build();
        } else {
            throw new UsernameNotFoundException("Invalid user request..!!");
        }
    }

    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
            .map(refreshToken -> {
                // Nếu không có thời gian hết hạn, không cần verifyExpiration
                String accessToken = jwtService.GenerateToken(refreshToken.getUser().getUsername());
                return JwtResponseDTO.builder()
                    .accessToken(accessToken)
                    .token(refreshTokenRequestDTO.getToken())
                    .build();
            }).orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }

    @PutMapping("/select-genres")
    public ResponseEntity<?> selectGenresForProfile(@RequestParam Long userId, @RequestBody GenresSelectedRequest request) {
        return new ResponseEntity<>(userService.updateSelectedGenres(userId, request), HttpStatus.CREATED);
    }
}
