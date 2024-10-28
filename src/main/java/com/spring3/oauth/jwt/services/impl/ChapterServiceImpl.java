package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Chapter;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.ChapterResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertChapterRequest;
import com.spring3.oauth.jwt.repositories.ChapterRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.services.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;

    @Override
    public List<ChapterResponseDTO> getAllChaptersInNovel(String slug) {
        if(chapterRepository.findAllByNovelSlug(slug).isEmpty()) {
            throw new NotFoundException("Chapter not found in novel with slug : " + slug);
        }
        return chapterRepository.findAllByNovelSlug(slug)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Override
    public ChapterResponseDTO getChapterByChapNoInNovel(String slug, int chapNo) {
        if(chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug) == null) {
            throw new NotFoundException("Chapter not found with chapter number: " + chapNo + " with novel slug: " + slug);
        }
        return convertToDTO(chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug));
    }

    @Override
    public ChapterResponseDTO saveChapter(UpsertChapterRequest request) {
        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new NotFoundException("Novel not found with id: " + request.getNovelId()));
        novel.setTotalChapters(novel.getTotalChapters() + 1);
        novelRepository.save(novel);

        Chapter chapter = new Chapter();
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle());
        chapter.setReleasedAt(LocalDateTime.now());
        chapter.setContentDoc(request.getContentDoc());
        chapter.setThumbnailImageUrl(request.getThumbnailImageUrl());
        chapter.setNovel(novel);
        return convertToDTO(chapterRepository.save(chapter));
    }

    @Override
    public ChapterResponseDTO updateChapter(int chapNo, String slug, UpsertChapterRequest request) {
        Chapter chapter = chapterRepository.findByChapterNoAndNovelSlug(chapNo, slug);

        if(chapter == null) {
            throw new NotFoundException("Chapter not found with chapter number: " + chapNo + " with novel slug : " + slug);
        }
        chapter.setChapterNo(request.getChapterNo());
        chapter.setTitle(request.getTitle());
        chapter.setContentDoc(request.getContentDoc());
        chapter.setThumbnailImageUrl(request.getThumbnailImageUrl());
        return convertToDTO(chapterRepository.save(chapter));
    }

    ChapterResponseDTO convertToDTO(Chapter chapter) {
        ChapterResponseDTO chapterResponseDTO = new ChapterResponseDTO();
        chapterResponseDTO.setId(chapter.getId());
        chapterResponseDTO.setChapterNo(chapter.getChapterNo());
        chapterResponseDTO.setTitle(chapter.getTitle());
        chapterResponseDTO.setReleasedAt(chapter.getReleasedAt());
        chapterResponseDTO.setContentDoc(chapter.getContentDoc());
        chapterResponseDTO.setThumbnailImageUrl(chapter.getThumbnailImageUrl());
        chapterResponseDTO.setNovelId(chapter.getNovel().getId());
        return chapterResponseDTO;
    }
}
