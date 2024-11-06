package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.request.CreateReadingLibraryRequest;
import com.spring3.oauth.jwt.services.ReadingLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reading-library")
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequiredArgsConstructor
public class ReadingLibraryController {

    private final ReadingLibraryService readingLibraryService;

    // Tạo mới một thư viện đọc cho người dùng
    @PostMapping("/create")
    public ResponseEntity<ReadingLibraryResponseDTO> createReadingLibrary(@RequestBody CreateReadingLibraryRequest request) {
        ReadingLibraryResponseDTO responseDTO = readingLibraryService.createReadingLibrary(request);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // Lấy thông tin thư viện đọc theo userId
    @GetMapping("/{userId}")
    public ResponseEntity<ReadingLibraryResponseDTO> getReadingLibraryByUserId(@PathVariable Integer userId) {
        ReadingLibraryResponseDTO responseDTO = readingLibraryService.getReadingLibraryByUserId(userId);
        return ResponseEntity.ok(responseDTO);
    }
}
