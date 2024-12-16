package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.UpsertChapterRequest;
import com.spring3.oauth.jwt.services.RedisService;
import com.spring3.oauth.jwt.services.impl.ChapterServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/chapters")
public class ChapterController {

    private final ChapterServiceImpl chapterService;
    private final RedisService redisService;

    @PostMapping("/thumbnail/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = chapterService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getAllChaptersInNovel(@PathVariable String slug) {
        return ResponseEntity.ok(chapterService.getAllChaptersInNovel(slug));
    }

    @GetMapping("/page/{slug}")
    public ResponseEntity<?> getAllChaptersPageInNovel(@PathVariable String slug,
                                                       @PageableDefault(size = 10,
                                                           sort = "chapterNo",
                                                           direction = Sort.Direction.ASC)
                                                       Pageable pageable) {
        return ResponseEntity.ok(chapterService.getAllChapterInNovelBySlug(slug, pageable));
    }

    @GetMapping("/{slug}/chap-{chapNo}")
    public ResponseEntity<?> getChapterDetailInNovel(@PathVariable String slug, @PathVariable int chapNo) {
        redisService.incrementApiCall("read-chapter");
        return ResponseEntity.ok(chapterService.getChapterByChapNoInNovel(slug, chapNo));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveChapter(@Valid @RequestBody UpsertChapterRequest request) {
        return ResponseEntity.ok(chapterService.saveChapter(request));
    }

    @PutMapping("/{slug}/chap-{chapNo}/update")
    @PreAuthorize("hasRole('ROLE_AUTHOR') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateChapter(@PathVariable String slug, @PathVariable int chapNo, @Valid @RequestBody UpsertChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapNo, slug, request));
    }

}
