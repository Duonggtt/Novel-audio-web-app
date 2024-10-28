package com.spring3.oauth.jwt.models.request;

import com.spring3.oauth.jwt.entity.Chapter;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.ReadingLibrary;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpsertReadingProgressRequest {
    private Integer readingLibraryId;
    private String slug;
    private Integer chapterId;
}
