package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_LIKES", indexes = {
    @Index(name = "idx_user_novel", columnList = "user_id, novel_id")
})
public class UserLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    private LocalDateTime likedAt;
}
