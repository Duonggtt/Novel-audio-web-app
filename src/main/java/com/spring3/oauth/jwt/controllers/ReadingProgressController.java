package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingProgressResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertReadingProgressRequest;
import com.spring3.oauth.jwt.services.ReadingProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/reading-progress")
public class ReadingProgressController {
    private final ReadingProgressService readingProgressService;

    @PostMapping("/create")
    public ResponseEntity<ReadingProgressResponseDTO> createReadingProgress(@RequestBody UpsertReadingProgressRequest request) {
        ReadingProgressResponseDTO responseDTO = readingProgressService.saveReadingProgress(request);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/library/{readingLibraryId}")
    public ResponseEntity<ReadingLibraryResponseDTO> getAllReadingProgressInLibrary(@PathVariable Integer readingLibraryId) {
        ReadingLibraryResponseDTO responseDTO = readingProgressService.getAllReadingProgressInLibrary(readingLibraryId);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/update")
    public ResponseEntity<ReadingProgressResponseDTO> updateReadingProgress(@RequestBody UpsertReadingProgressRequest request) {
        ReadingProgressResponseDTO responseDTO = readingProgressService.updateReadingProgress(request);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/get-reading-progress")
    public ResponseEntity<ReadingProgressResponseDTO> getReadingProgress(
        @RequestParam Integer readingLibraryId,
        @RequestParam String slug) {
        ReadingProgressResponseDTO responseDTO = readingProgressService.getReadingProgress(readingLibraryId, slug);
        return ResponseEntity.ok(responseDTO);
    }
}
