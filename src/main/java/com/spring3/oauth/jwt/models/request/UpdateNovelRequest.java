package com.spring3.oauth.jwt.models.request;

import com.spring3.oauth.jwt.entity.enums.NovelStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateNovelRequest {
    private String title;
    private String description;
    private NovelStatusEnum status;
    private String thumbnailImageUrl;
    private boolean isClosed;
    private Integer authorId;
    private List<Integer> genreIds;
}
