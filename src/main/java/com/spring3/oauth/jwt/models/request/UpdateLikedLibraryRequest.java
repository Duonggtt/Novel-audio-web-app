package com.spring3.oauth.jwt.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateLikedLibraryRequest {
    private Integer userId;
    private String slug;
}
