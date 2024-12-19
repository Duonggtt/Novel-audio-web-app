package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.LikedLibraryResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateLikedLibraryRequest;
import org.springframework.data.domain.Pageable;

public interface LikedLibraryService {
    LikedLibraryResponseDTO getLikedLibraryByUserId(Integer userId);
    void deleteLikedLibrary(Integer userId, Integer novelId);
    boolean addNovelToLikedLibrary(UpdateLikedLibraryRequest request);
    void createLikedLibrary(long UserId);
}
