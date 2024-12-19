package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Genre;
import com.spring3.oauth.jwt.entity.LikedLibrary;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.LikedLibraryResponseDTO;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateLikedLibraryRequest;
import com.spring3.oauth.jwt.repositories.LikedLibraryRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.LikedLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LikedLibraryServiceImpl implements LikedLibraryService {

    private final LikedLibraryRepository likedLibraryRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    @Override
    public LikedLibraryResponseDTO getLikedLibraryByUserId(Integer userId) {
        LikedLibrary likedLibrary = likedLibraryRepository.findByUser_Id(userId);
        if(likedLibrary == null) {
            throw new NotFoundException("Liked Library not found with user id: " + userId);
        }
        return convertToLikedDto(likedLibrary);
    }

    @Override
    public void deleteLikedLibrary(Integer userId, Integer novelId) {
        LikedLibrary likedLibrary = likedLibraryRepository.findByUser_Id(userId);
        if(likedLibrary == null) {
            throw new NotFoundException("Liked Library not found with user id: " + userId);
        }
        List<Novel> novels = likedLibrary.getNovels();
        novels.removeIf(novel -> novel.getId() == novelId);
        likedLibrary.setNovels(novels);
        likedLibraryRepository.save(likedLibrary);
    }

    @Override
    public boolean addNovelToLikedLibrary(UpdateLikedLibraryRequest request) {
        LikedLibrary likedLibrary = likedLibraryRepository.findByUser_Id(request.getUserId());
        if(likedLibrary == null) {
            createLikedLibrary(request.getUserId());
        }
        List<Novel> novels = likedLibrary.getNovels();
        Novel novel = novelRepository.findBySlug(request.getSlug());
        if(novel == null) {
            throw new NotFoundException("Novel not found with slug: " + request.getSlug());
        }
        if (novels.contains(novel)) {
            novels.remove(novel);
            likedLibrary.setNovels(novels);
            likedLibraryRepository.save(likedLibrary);
            return false;
        } else {
            novels.add(novel);
            likedLibrary.setNovels(novels);
            likedLibraryRepository.save(likedLibrary);
            return true;
        }
    }

    @Override
    public void createLikedLibrary(long UserId) {
        LikedLibrary likedLibraryCheck = likedLibraryRepository.findByUser_Id((int) UserId);
        if(likedLibraryCheck != null) {
            throw new NotFoundException("Liked Library already exists with user id: " + UserId);
        }
        User user = userRepository.findById(UserId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + UserId));
        LikedLibrary likedLibrary = new LikedLibrary();
        likedLibrary.setUser(user);
        likedLibrary.setNovels(null);
        likedLibraryRepository.save(likedLibrary);
    }

    LikedLibraryResponseDTO convertToLikedDto(LikedLibrary likedLibrary) {
        LikedLibraryResponseDTO dto = new LikedLibraryResponseDTO();
        dto.setId(likedLibrary.getId());
        dto.setUserId((int) likedLibrary.getUser().getId());
        List<NovelResponseDTO> novelResponseDTOs = likedLibrary.getNovels()
            .stream()
            .map(this::convertToDto)
            .toList();
        dto.setContent(novelResponseDTOs);
        return dto;
    }

    NovelResponseDTO convertToDto(Novel novel) {
        NovelResponseDTO dto = new NovelResponseDTO();
        dto.setTitle(novel.getTitle());
        dto.setSlug(novel.getSlug());
        dto.setDescription(novel.getDescription());
        dto.setReleasedAt(novel.getReleasedAt());
        dto.setStatus(novel.getStatus());
        dto.setClosed(novel.isClosed());
        dto.setThumbnailImageUrl(novel.getThumbnailImageUrl());
        dto.setReadCounts(novel.getReadCounts());
        dto.setTotalChapters(novel.getTotalChapters());
        dto.setAverageRatings(novel.getAverageRatings());
        dto.setLikeCounts(novel.getLikeCounts());
        if(novel.getAuthor() == null) {
            dto.setAuthorName(null);
        }else {
            dto.setAuthorName(novel.getAuthor().getFullName());
        }
        if(novel.getGenres() == null) {
            dto.setGenreNames(null);
        }
        else {
            dto.setGenreNames(novel.getGenres()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
