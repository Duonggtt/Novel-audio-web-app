package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.AudioFile;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.models.dtos.AudioFileResponseDTO;
import com.spring3.oauth.jwt.repositories.AudioFileRepository;
import com.spring3.oauth.jwt.services.AudioFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AudioFileServiceImpl implements AudioFileService{

    private final AudioFileRepository audioFileRepository;

    @Override
    public AudioFileResponseDTO getAudioFileByChapterId(Integer chapterId) {
        if(audioFileRepository.findByChapterId(chapterId) == null) {
            throw new NotFoundException("Audio file not found with chapter id: " + chapterId);
        }
        return convertToDto(audioFileRepository.findByChapterId(chapterId));
    }

    AudioFileResponseDTO convertToDto(AudioFile audioFile) {
        return AudioFileResponseDTO.builder()
            .id(audioFile.getId())
            .chapterId(audioFile.getChapter().getId())
            .audioUrl(audioFile.getAudioUrl())
            .duration(audioFile.getDuration())
            .build();
    }
}
