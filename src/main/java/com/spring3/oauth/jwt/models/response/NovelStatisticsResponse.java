package com.spring3.oauth.jwt.models.response;

import com.spring3.oauth.jwt.models.dtos.NovelStatusResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NovelStatisticsResponse {
    private List<NovelStatusResponseDTO> week;
    private List<NovelStatusResponseDTO> year;
}
