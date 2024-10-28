package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.dtos.ReadingProgressResponseDTO;
import com.spring3.oauth.jwt.models.request.UpsertReadingProgressRequest;

public interface ReadingProgressService {
    ReadingProgressResponseDTO saveReadingProgress(UpsertReadingProgressRequest request);
    ReadingProgressResponseDTO updateReadingProgress(UpsertReadingProgressRequest request);
    ReadingProgressResponseDTO getReadingProgress(Integer readingLibraryId, String slug);
    ReadingLibraryResponseDTO getAllReadingProgressInLibrary(Integer readingLibraryId);
}
