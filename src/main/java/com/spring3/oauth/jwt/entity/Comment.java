package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "COMMNENTS")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "novel_id")
    private Novel novel;

    // Nhận biết binh luận nào là cha nào là con
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true) // Bình luận gốc sẽ có parent_id là null
    private Comment parent;
}
