package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.request.UpdateLikedLibraryRequest;
import com.spring3.oauth.jwt.services.impl.LikedLibraryServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/liked-novels")
public class LikedNovelController {
    private final LikedLibraryServiceImpl likedLibraryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllLikedNovelsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(likedLibraryService.getLikedLibraryByUserId(userId));
    }

    @DeleteMapping("/user/{userId}/novel/{novelId}")
    public ResponseEntity<?> deleteLikedNovel(@PathVariable Integer userId, @PathVariable Integer novelId) {
        likedLibraryService.deleteLikedLibrary(userId, novelId);
        return ResponseEntity.ok("Novel deleted successfully");
    }

    @PutMapping("/save-to-library")
    public ResponseEntity<?> addNovelToLikedLibrary(@Valid @RequestBody UpdateLikedLibraryRequest request) {
        return ResponseEntity.ok(likedLibraryService.addNovelToLikedLibrary(request));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLikedLibrary(@RequestParam long userId) {
        likedLibraryService.createLikedLibrary(userId);
        return ResponseEntity.ok("Liked Library created successfully");
    }

}
