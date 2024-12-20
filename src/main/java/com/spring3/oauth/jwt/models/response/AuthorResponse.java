package com.spring3.oauth.jwt.models.response;

import com.spring3.oauth.jwt.entity.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private UserStatusEnum accountStatus;
}
