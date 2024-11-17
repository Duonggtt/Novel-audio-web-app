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

    // Những người đang follow user này (nếu user là author)
    @JsonIgnore
    @ManyToMany(fetch = LAZY)
    @JoinTable(
        name = "user_followers",
        joinColumns = @JoinColumn(name = "author_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<User> followers = new HashSet<>();

    // Những author mà user này đang follow
    @JsonIgnore
    @ManyToMany(mappedBy = "followers", fetch = LAZY)
    private Set<User> following = new HashSet<>();

    @Column(name = "account_status")
    private UserStatusEnum status;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "chapter_read_count")
    private int chapterReadCount;

    @Column(name = "point")
    private int point = 1;

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

    @ManyToMany(fetch = LAZY)
    private List<Hobby> hobbies;

    // Helper methods for managing followers
    public void addFollower(User follower) {
        followers.add(follower);
        follower.getFollowing().add(this);
    }

    public void removeFollower(User follower) {
        followers.remove(follower);
        follower.getFollowing().remove(this);
    }

    // Method to check if user is an author
    public boolean isAuthor() {
        return roles.stream()
            .anyMatch(role -> "ROLE_AUTHOR".equals(role.getName()));
    }

    // Get follower count
    public int getFollowerCount() {
        return followers.size();
    }
}
