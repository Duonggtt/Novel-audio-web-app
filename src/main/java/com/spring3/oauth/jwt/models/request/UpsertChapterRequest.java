package com.spring3.oauth.jwt.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpsertChapterRequest {
    private int chapterNo;
    private String title;
    private String contentDoc;
    private String thumbnailImageUrl;
    private String slug;
}
