package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.models.dtos.ReadingLibraryResponseDTO;
import com.spring3.oauth.jwt.models.request.CreateReadingLibraryRequest;

public interface ReadingLibraryService {
    ReadingLibraryResponseDTO createReadingLibrary(CreateReadingLibraryRequest request);
    ReadingLibraryResponseDTO getReadingLibraryByUserId(Integer userId);
}
