package com.spring3.oauth.jwt.controllers;


import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.models.request.UpdateNovelRequest;
import com.spring3.oauth.jwt.models.request.UpsertNovelRequest;
import com.spring3.oauth.jwt.services.RedisService;
import com.spring3.oauth.jwt.services.impl.NovelServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/novels")
public class NovelController {

    private final NovelServiceImpl novelService;
    private final RedisService redisService;

    @GetMapping("/")
    public ResponseEntity<?> getAllNovels() {
        return ResponseEntity.ok(novelService.getAllNovelDtos());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getNovelBySlug(@PathVariable String slug, @RequestParam long userId) {
        return ResponseEntity.ok(novelService.getDetailNovel(slug, userId));
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

    @GetMapping("/bxh/top-read")
    public ResponseEntity<?> getBxhTopReadNovels(
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
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveNovel(@Valid @RequestBody UpsertNovelRequest request) {
        NovelResponseDTO novel = novelService.saveNovel(request);
        return new ResponseEntity<>(novel, HttpStatus.CREATED);
    }

    @PutMapping("/update/{slug}")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateNovel(@PathVariable String slug, @Valid @RequestBody UpdateNovelRequest request) {
        NovelResponseDTO novel = novelService.updateNovel(slug, request);
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

    @GetMapping("/auth/my-novels")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> searchNovelsByAuthorAuthName(@RequestParam String authorName, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByAuthorAuthName(authorName, pageable));
    }


    @GetMapping("/search/by-author-id/{authorId}")
    public ResponseEntity<?> searchNovelsByAuthorId(@PathVariable Integer authorId, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByAuthorId(authorId, pageable));
    }

    @GetMapping("/search/by-title")
    public ResponseEntity<?> searchNovelsByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(novelService.findAllByTitle(title, pageable));
    }

    // API kiểm tra trạng thái like của người dùng cho một truyện
    @GetMapping("/{novelSlug}/is-liked")
    public boolean isNovelLiked(@PathVariable String novelSlug, @RequestParam long userId) {
        return novelService.isNovelLikedByUser(userId, novelSlug);
    }

    // API lấy danh sách các truyện mà người dùng đã like
    @GetMapping("/liked-novels")
    public List<String> getLikedNovelsByUser(@RequestParam long userId) {
        return novelService.getLikedNovelSlugsByUser(userId);
    }

    // API để "like" truyện dựa trên slug
    @PostMapping("/like/{slug}")
    public ResponseEntity<Boolean> likeNovel(@PathVariable String slug, @RequestParam long userId) {
        boolean isLiked = novelService.likeNovel(userId, slug);
        return ResponseEntity.ok(isLiked);
    }
}
