package com.spring3.oauth.jwt.models.dtos;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResultDTO<T> {
    private List<T> items;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}