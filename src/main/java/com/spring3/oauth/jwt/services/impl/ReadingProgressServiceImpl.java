package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.*;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingProgressResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertReadingProgressRequest;
import com.spring3.oauth.jwt.repositories.ChapterRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.repositories.ReadingLibraryRepository;
import com.spring3.oauth.jwt.repositories.UserReadingProgressRepository;
import com.spring3.oauth.jwt.services.ReadingProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReadingProgressServiceImpl implements ReadingProgressService {
    private final UserReadingProgressRepository userReadingProgressRepository;
    private final ReadingLibraryRepository readingLibraryRepository;
    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;

    @Override
    public ReadingProgressResponseDTO saveReadingProgress(UpsertReadingProgressRequest request) {
        ReadingLibrary readingLibrary = readingLibraryRepository.findById(request.getReadingLibraryId())
            .orElseThrow(() -> new NotFoundException("Reading library not found"));

        Novel novel = novelRepository.findBySlug(request.getSlug());
        if (novel == null) {
            throw new NotFoundException("Novel not found");
        }

        if (userReadingProgressRepository.findByNovel_SlugAndReadingLibrary_Id(novel.getSlug(), readingLibrary.getId()) != null) {
            throw new IllegalArgumentException("Reading progress already exists");
        }


        Chapter chapter = chapterRepository.findById(request.getChapterId())
            .orElseThrow(() -> new NotFoundException("Chapter not found"));

        if (!chapter.getNovel().getSlug().equals(novel.getSlug())) {
            throw new NotFoundException("Chapter does not belong to the specified novel");
        }

        UserReadingProgress userReadingProgress = new UserReadingProgress();
        userReadingProgress.setReadingLibrary(readingLibrary);
        userReadingProgress.setNovel(novel);
        userReadingProgress.setLastReadChapter(chapter);

        return convertToDto(userReadingProgressRepository.save(userReadingProgress));
    }

    @Override
    public ReadingProgressResponseDTO updateReadingProgress(UpsertReadingProgressRequest request) {
        UserReadingProgress userReadingProgress = userReadingProgressRepository.findByNovel_SlugAndReadingLibrary_Id(request.getSlug(), request.getReadingLibraryId());
        if (userReadingProgress == null) {
            throw new NotFoundException("Reading progress not found");
        }

        Novel novel = novelRepository.findBySlug(request.getSlug());
        if (novel == null) {
            throw new NotFoundException("Novel not found");
        }

        Chapter chapter = chapterRepository.findById(request.getChapterId())
            .orElseThrow(() -> new NotFoundException("Chapter not found"));

        if (!chapter.getNovel().getSlug().equals(novel.getSlug())) {
            throw new NotFoundException("Chapter does not belong to the specified novel");
        }

        // Nếu chương mới khác với chương hiện tại, cập nhật chương cuối đã đọc
        if (userReadingProgress.getLastReadChapter().getId() != chapter.getId()) {
            userReadingProgress.setLastReadChapter(chapter);
        }


        return convertToDto(userReadingProgressRepository.save(userReadingProgress));
    }

    @Override
    public ReadingProgressResponseDTO getReadingProgress(Integer readingLibraryId, String slug) {
        UserReadingProgress readingProgress = userReadingProgressRepository.findByNovel_SlugAndReadingLibrary_Id(slug, readingLibraryId);
        if (readingProgress == null) {
            throw new NotFoundException("Reading progress not found");
        }
        return convertToDto(readingProgress);
    }

    @Override
    public ReadingLibraryResponseDTO getAllReadingProgressInLibrary(Integer readingLibraryId) {
        ReadingLibrary readingLibrary = readingLibraryRepository.findById(readingLibraryId)
            .orElseThrow(() -> new NotFoundException("Reading library not found"));

        // Lấy toàn bộ tiến độ đọc trong thư viện đọc
        List<UserReadingProgress> readingProgressList = userReadingProgressRepository.findAllByReadingLibraryId(readingLibraryId);

        List<ReadingProgressResponseDTO> readingProgressResponseList = readingProgressList.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        ReadingLibraryResponseDTO responseDTO = new ReadingLibraryResponseDTO();
        responseDTO.setId(readingLibrary.getId());
        responseDTO.setUserId((int) readingLibrary.getUser().getId());
        responseDTO.setReadingProgressList(readingProgressResponseList);

        return responseDTO;
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
