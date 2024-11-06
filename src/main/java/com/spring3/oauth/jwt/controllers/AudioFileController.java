package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.services.impl.AudioFileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/audio-files")
public class AudioFileController {

    private final AudioFileServiceImpl audioFileService;

    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getAudioFileByChapterId(@PathVariable Integer chapterId) {
        return ResponseEntity.ok(audioFileService.getAudioFileByChapterId(chapterId));
    }

}
