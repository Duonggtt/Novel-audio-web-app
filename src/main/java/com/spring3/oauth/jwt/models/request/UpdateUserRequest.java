package com.spring3.oauth.jwt.models.request;

import com.spring3.oauth.jwt.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateUserRequest {

    private String fullName;
    private String email;
    private LocalDate dob;
}
