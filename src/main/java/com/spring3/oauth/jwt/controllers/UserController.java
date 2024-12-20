package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.RefreshToken;
import com.spring3.oauth.jwt.entity.Role;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.dtos.*;
import com.spring3.oauth.jwt.models.request.*;
import com.spring3.oauth.jwt.models.response.UserResponse;
import com.spring3.oauth.jwt.repositories.RoleRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.EmailService;
import com.spring3.oauth.jwt.services.JwtService;
import com.spring3.oauth.jwt.services.RefreshTokenService;
import com.spring3.oauth.jwt.services.UserService;
import com.spring3.oauth.jwt.services.impl.CoinWalletServiceImpl;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
public class UserController {

    @Autowired
    UserService userService;

    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserRepository userRepository;
    @Autowired
    private CoinWalletServiceImpl coinWalletServiceImpl;

    public UserController(EmailService emailService, RoleRepository roleRepository, RestTemplate restTemplate, UserRepository userRepository) {
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("/send-update-role-email")
    public ResponseEntity<String> sendAuthorRequest(
            @RequestParam String fromEmail,
            @RequestParam long userId,
            @RequestParam String username) {
        try {
            emailService.sendForAcceptAuthorRequest(fromEmail, userId, username);
            return ResponseEntity.ok("Author request email sent successfully.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send author request email.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/mail-role/accept")
    public ResponseEntity<String> acceptAuthorRequest(@RequestParam long userId, @RequestParam String username) throws MessagingException {
        // Update user role here
        User user = userRepository.findByUsernameAndId(username, userId);
        if (user != null) {
            Set<Role> roles = roleRepository.findAllByName("ROLE_AUTHOR");
            userRepository.save(user);

            // Send acceptance notification
            emailService.sendNotificationToUser(user.getEmail(), "Congratulations", "You have been Accepted to be an author.");

            // Return HTML response indicating success
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(generateHtmlResponse("Accepted!", "You accepted the request to become an author."));
        }
        // Return HTML response indicating failure
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_HTML)
                .body(generateHtmlResponse("User Not Found", "The user could not be found. Please try again."));
    }

    @GetMapping("/mail-role/decline")
    public ResponseEntity<String> declineAuthorRequest(@RequestParam long userId, @RequestParam String username) throws MessagingException {
        User user = userRepository.findByUsernameAndId(username, userId);
        if (user != null) {
            // Send rejection notification
            emailService.sendNotificationToUser(user.getEmail(), "Request Declined", "Your request to be an author has been Declined.");

            // Return HTML response indicating rejection
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(generateHtmlResponse("Declined", "You declined the request to be an author."));
        }
        // Return HTML response indicating failure
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_HTML)
                .body(generateHtmlResponse("User Not Found", "The user could not be found. Please try again."));
    }

    @GetMapping("/mail-role/acceptRedirect")
    public ResponseEntity<String> acceptAuthorRequestRedirect(@RequestParam long userId, @RequestParam String username) {
        // Gọi POST nội bộ đến endpoint chấp nhận
        return sendInternalGetRequest("/api/v1/mail-role/accept", userId, username);
    }

    @GetMapping("/mail-role/declineRedirect")
    public ResponseEntity<String> declineAuthorRequestRedirect(@RequestParam long userId, @RequestParam String username) {
        // Gọi POST nội bộ đến endpoint từ chối
        return sendInternalGetRequest("/api/v1/mail-role/decline", userId, username);
    }

    // Hàm nội bộ để gửi POST request
    private ResponseEntity<String> sendInternalGetRequest(String url, long userId, String username) {
        try {
            String fullUrl = "http://localhost:9898" + url + "?userId=" + userId + "&username=" + username;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Send GET request
            return restTemplate.exchange(fullUrl, HttpMethod.GET, requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process request: " + e.getMessage());
        }
    }

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

    @PutMapping("/wallet/add-coin")
    public ResponseEntity<?> addCoinToWallet(@RequestParam int coin, @RequestParam long userId) {
        coinWalletServiceImpl.addCoinToWallet(coin, userId);
        return ResponseEntity.ok("Coin added to wallet successfully.");
    }

    @PutMapping("/user/add-point")
    public ResponseEntity<?> addPoint(@RequestParam int point, @RequestParam long userId) {
        userService.addPoint(point, userId);
        return ResponseEntity.ok("Point added successfully.");
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

    @PutMapping("/auth/set-account-status")
    public ResponseEntity<?> setAccountStatus(@RequestParam String username) {
        return ResponseEntity.ok(userService.setStatusUser(username));
    }


    @PostMapping(value = "/save-author")
    public ResponseEntity<?> saveAuthor(@RequestBody AuthorRequest request) {
        try {
            UserResponse userResponse = userService.saveAuthor(request);
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
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "0") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<UserResponse> userResponses = userService.getAllUser(pageable);
            UserResponsePageDTO response = new UserResponsePageDTO(userResponses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/page/authors")
    public ResponseEntity<?> getAllAuthors(
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize) {

        // Chuẩn bị DTO phân trang
        PaginationDTO paginationDTO = PaginationDTO.builder()
            .pageNum(pageNum)
            .pageSize(pageSize)
            .build();

        // Gọi Service và trả về kết quả
        return ResponseEntity.ok(userService.getAllAuthor(paginationDTO));
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
            String role = userService.getRole(authRequestDTO.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            long userId = userService.getUserIdByUsername(authRequestDTO.getUsername());
            return JwtResponseDTO.builder()
                .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                .token(refreshToken.getToken())
                .role(role)
                .userId(userId)
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

    @GetMapping("/bxh/top-read")
    public ResponseEntity<?> getTopRead() {
        return ResponseEntity.ok(userService.getTopRead());
    }
    
    @GetMapping("/bxh/top-point")
    public ResponseEntity<?> getTopPoint() {
        return ResponseEntity.ok(userService.getTopPoint());
    }

    @PostMapping("/follow/author")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> followAuthor(@RequestParam String currentUsername, @RequestParam long authorId) {
        userService.followAuthor(currentUsername, authorId);
        return ResponseEntity.ok("Followed author successfully.");
    }

    @GetMapping("/authors/")
    public ResponseEntity<?> getAuthors() {
        return ResponseEntity.ok(userService.getAuthors());
    }

    @PostMapping("/unfollow/author")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> unfollowAuthor(@RequestParam String currentUsername, @RequestParam long authorId) {
        userService.unfollowAuthor(currentUsername, authorId);
        return ResponseEntity.ok("Unfollowed author successfully.");
    }

    @GetMapping("/followers/{authorId}")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAuthorFollowers(@PathVariable long authorId) {
        return ResponseEntity.ok(userService.getAuthorFollowers(authorId));
    }

    @GetMapping("/following/{username}")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getUserFollowing(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserFollowing(username));
    }

    @GetMapping("/followers/count/{authorId}")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> getFollowerCount(@PathVariable long authorId) {
        return ResponseEntity.ok(userService.getFollowerCount(authorId));
    }

    private String generateHtmlResponse(String title, String message) {
        String logoUrl = "http://14.225.207.58:3000/photo/dd08665ea32a0a8ee80f.png";

        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #ffffff; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                ".container { text-align: center; padding: 20px; border-radius: 8px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1); background-color: #ffffff; max-width: 400px; width: 100%; }" +
                "img { max-width: 100px; margin-bottom: 20px; }" +
                "h1 { font-size: 24px; color: #333; margin-bottom: 10px; }" +
                "p { font-size: 18px; color: #555; margin-top: 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<img src='" + logoUrl + "' alt='Logo'>" +
                "<h1>" + title + "</h1>" +
                "<p>" + message + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
