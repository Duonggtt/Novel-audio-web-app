package com.spring3.oauth.jwt.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthorRequest {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private LocalDate dob;
    private String email;
}
