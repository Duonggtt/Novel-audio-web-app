package com.spring3.oauth.jwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring3.oauth.jwt.entity.enums.UserStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;


@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private long id;

    private String username;

    @JsonIgnore
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "account_status")
    private UserStatusEnum status;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "chapter_read_count")
    private int chapterReadCount;

    @Column(name = "dob")
    private LocalDate dob;

    private String email;
    
    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "tier_id")
    private Tier tier;

    @ManyToMany(fetch = LAZY)
    private List<Genre> selectedGenres;

}
