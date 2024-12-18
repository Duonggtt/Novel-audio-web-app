package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.*;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingProgressResponseDTO;
import com.spring3.oauth.jwt.models.request.CreateReadingLibraryRequest;
import com.spring3.oauth.jwt.repositories.ReadingLibraryRepository;
import com.spring3.oauth.jwt.repositories.UserReadingProgressRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.ReadingLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReadingLibraryServiceImpl implements ReadingLibraryService {

    private final ReadingLibraryRepository readingLibraryRepository;
    private final UserRepository userRepository;
    private final UserReadingProgressRepository userReadingProgressRepository;

    @Override
    public ReadingLibraryResponseDTO createReadingLibrary(CreateReadingLibraryRequest request) {
        ReadingLibrary readingLibrary = new ReadingLibrary();
        User user = userRepository.findById(Long.valueOf(request.getUserId()))
            .orElseThrow(() -> new NotFoundException("User not found"));
        readingLibrary.setUser(user);
        readingLibraryRepository.save(readingLibrary);
        return convertToDto(readingLibrary);
    }

    @Override
    public ReadingLibraryResponseDTO getReadingLibraryByUserId(Integer userId) {
        ReadingLibrary readingLibrary = readingLibraryRepository.findByUser_Id(userId);
        if (readingLibrary == null) {
            throw new NotFoundException("Reading library not found");
        }
        return convertToDto(readingLibrary);
    }

    ReadingLibraryResponseDTO convertToDto(ReadingLibrary readingLibrary) {
        ReadingLibraryResponseDTO dto = new ReadingLibraryResponseDTO();
        List<UserReadingProgress> list = userReadingProgressRepository.findAllByReadingLibraryId(readingLibrary.getId());
        dto.setId(readingLibrary.getId());
        dto.setUserId((int) readingLibrary.getUser().getId());
        dto.setReadingProgressList(list.stream().map(this::convertToDto).collect(Collectors.toList()));
        return dto;
    }

    private ReadingProgressResponseDTO convertToDto(UserReadingProgress userReadingProgress) {
        Chapter chapter = userReadingProgress.getLastReadChapter();
        Novel novel = userReadingProgress.getNovel();
        ReadingProgressResponseDTO responseDTO = new ReadingProgressResponseDTO();
        responseDTO.setId(userReadingProgress.getId());
        responseDTO.setReadingLibraryId(userReadingProgress.getReadingLibrary().getId());
        responseDTO.setNovel(convertToNovelDto(novel));
        responseDTO.setLastReadChapterNo(chapter.getChapterNo());  // Gán số chương cuối cùng
        return responseDTO;
    }


    private NovelResponseDTO convertToNovelDto(Novel novel) {
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
        if (novel.getAuthor() == null) {
            dto.setAuthorName(null);
        } else {
            dto.setAuthorName(novel.getAuthor().getFullName());
        }
        if (novel.getGenres() == null) {
            dto.setGenreNames(null);
        } else {
            dto.setGenreNames(novel.getGenres()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
