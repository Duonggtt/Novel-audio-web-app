package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.ChapterResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PagedChapterResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertChapterRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChapterService {
    List<ChapterResponseDTO> getAllChaptersInNovel(String slug);
    ChapterResponseDTO getChapterByChapNoInNovel(String slug, int chapNo);
    ChapterResponseDTO saveChapter(UpsertChapterRequest request);
    ChapterResponseDTO updateChapter(int chapNo, String slug, UpsertChapterRequest request);
    PagedChapterResponseDTO getAllChapterInNovelBySlug(String slug, Pageable pageable);
}
