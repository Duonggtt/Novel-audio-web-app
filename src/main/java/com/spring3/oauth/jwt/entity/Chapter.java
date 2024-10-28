package com.spring3.oauth.jwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CHAPTERS")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "chapter_no")
    private int chapterNo;

    @Column(name = "content_doc", columnDefinition = "LONGTEXT")
    private String contentDoc;

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @ManyToOne
    @JoinColumn(name = "novel_id")
    private Novel novel;
}
