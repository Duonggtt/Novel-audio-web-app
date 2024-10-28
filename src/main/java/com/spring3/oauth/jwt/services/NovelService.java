package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.dtos.PagedResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertNovelRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NovelService {
    List<Novel> getAllNovels();
    List<NovelResponseDTO> getAllNovelDtos();
    NovelResponseDTO getNovelById(Integer id);
    NovelResponseDTO saveNovel(UpsertNovelRequest request);
    NovelResponseDTO updateNovel(Integer novelId, UpsertNovelRequest request);
    void deleteNovel(Integer id);
    PagedResponseDTO getAllTrendingNovels(Pageable pageable);
    PagedResponseDTO getAllTopNovels(Pageable pageable);
    PagedResponseDTO findAllByReleasedAtWithinLast7Days(Pageable pageable);
    NovelResponseDTO getDetailNovel(String slug);
    NovelResponseDTO updateLikeCount(String slug);
    PagedResponseDTO findAllByGenre(List<Integer> genreIds, Pageable pageable);
    List<NovelResponseDTO> findSomeNovelsSameGenre(Integer genreId);
    PagedResponseDTO getAllNovelsByGenreName(String genreName, Pageable pageable);
    PagedResponseDTO findAllByAuthorName(String authorName, Pageable pageable);
    PagedResponseDTO findAllByAuthorId(Integer authorId, Pageable pageable);
    PagedResponseDTO findAllByTitle(String title, Pageable pageable);
    PagedResponseDTO getAllNovelsRecommend( Long userId, Pageable pageable);
}
