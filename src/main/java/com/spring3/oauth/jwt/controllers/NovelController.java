package com.spring3.oauth.jwt.controllers;


import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertNovelRequest;
import com.spring3.oauth.jwt.services.impl.NovelServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/novels")
public class NovelController {

    private final NovelServiceImpl novelService;

    @GetMapping("/")
    public ResponseEntity<?> getAllNovels() {
        return ResponseEntity.ok(novelService.getAllNovelDtos());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getNovelBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(novelService.getDetailNovel(slug));
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingNovels(
        @PageableDefault(size = 10, sort = "likeCounts", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(novelService.getAllTrendingNovels(pageable));
    }

    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommendNovels(
        @RequestParam Long userId,
        @PageableDefault(size = 10, sort = "likeCounts", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(novelService.getAllNovelsRecommend(userId, pageable));
    }

    @GetMapping("/top-read")
    public ResponseEntity<?> getTopReadNovels(
        @PageableDefault(size = 10, sort = "readCounts", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(novelService.getAllTopNovels(pageable));
    }

    @GetMapping("/new-released")
    public ResponseEntity<?> getNovelsReleasedLast7Days(
        @PageableDefault(size = 10, sort = "releasedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByReleasedAtWithinLast7Days(pageable));
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<?> getNovelsByGenreName(@PathVariable String genreName,
        @PageableDefault(size = 10, sort = "likeCounts", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(novelService.getAllNovelsByGenreName(genreName, pageable));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveNovel(@Valid @RequestBody UpsertNovelRequest request) {
        NovelResponseDTO novel = novelService.saveNovel(request);
        return new ResponseEntity<>(novel, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNovel(@PathVariable Integer id, @Valid @RequestBody UpsertNovelRequest request) {
        NovelResponseDTO novel = novelService.updateNovel(id, request);
        return new ResponseEntity<>(novel, HttpStatus.OK);
    }

    @PutMapping("/like-count-update/{slug}")
    public ResponseEntity<?> updateLikeCount(@PathVariable String slug) {
        NovelResponseDTO novel = novelService.updateLikeCount(slug);
        return new ResponseEntity<>(novel, HttpStatus.OK);
    }

    @GetMapping("/filter-by-genre")
    public ResponseEntity<?> getAllNovelsByGenre(@RequestParam List<Integer> genreIds, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByGenre(genreIds, pageable));
    }

    @GetMapping("/search/by-author")
    public ResponseEntity<?> searchNovelsByAuthorName(@RequestParam String authorName, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByAuthorName(authorName, pageable));
    }

    @GetMapping("/search/by-author-id/{authorId}")
    public ResponseEntity<?> searchNovelsByAuthorId(@PathVariable Integer authorId, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByAuthorId(authorId, pageable));
    }

    @GetMapping("/search/by-title")
    public ResponseEntity<?> searchNovelsByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByTitle(title, pageable));
    }
}
