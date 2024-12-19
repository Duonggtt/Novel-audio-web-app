package com.spring3.oauth.jwt.models.dtos;

import com.spring3.oauth.jwt.models.response.UserResponse;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class UserResponsePageDTO {
    private List<UserResponse> userResponses;
    private int pageNum;
    private int pageSize;
    private long totalItems;

    public UserResponsePageDTO(Page<UserResponse> page) {
        this.userResponses = page.getContent();
        this.pageNum = page.getNumber();
        this.pageSize = page.getSize();
        this.totalItems = page.getTotalElements();
    }
}
