package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.AudioFileResponseDTO;

public interface AudioFileService {
    AudioFileResponseDTO getAudioFileByChapterId(Integer chapterId);
}
