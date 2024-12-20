package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.*;
import com.spring3.oauth.jwt.models.request.*;
import com.spring3.oauth.jwt.models.response.AuthorResponse;
import com.spring3.oauth.jwt.models.response.UserResponse;
import com.spring3.oauth.jwt.repositories.itf.AuthorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;


public interface UserService {

    UserResponse saveUser(UserRequest userRequest);
    UserResponse saveAuthor(AuthorRequest authorRequest);

    UserResponse getUser();
    boolean setStatusUser(String username);
    UserResponseDTO getProfile(String username);

    Page<UserResponse> getAllUser(Pageable pageable);

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
    void followAuthor(String currentUsername, long authorId);
    void unfollowAuthor(String currentUsername, long authorId);
    List<FollowResponseDTO> getAuthorFollowers(long authorId);
    List<FollowResponseDTO> getUserFollowing(String username);
    int getFollowerCount(long authorId);
    Map<String, Integer> getAllUsersQuantityForEachLevel();
    Map<String, Integer> getUserCountByScoreRange();
    Map<String, Integer> getTotalLikeCountsForLastWeek();

    void addPoint(int point, long userId);
    PagedResultDTO<AuthorResponse> getAllAuthor(PaginationDTO paginationDTO);
    List<AuthorResponseDTO> getAuthors();
}
