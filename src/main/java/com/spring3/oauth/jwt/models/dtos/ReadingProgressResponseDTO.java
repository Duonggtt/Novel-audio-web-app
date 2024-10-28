package com.spring3.oauth.jwt.models.dtos;

import com.spring3.oauth.jwt.entity.Chapter;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.ReadingLibrary;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadingProgressResponseDTO {
    private Integer id;
    private Integer readingLibraryId;
    private NovelResponseDTO novel;
    private int lastReadChapterNo;
}
