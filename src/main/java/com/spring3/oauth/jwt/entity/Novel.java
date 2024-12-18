package com.spring3.oauth.jwt.entity;

import com.spring3.oauth.jwt.entity.enums.NovelStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Data
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "NOVELS")
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String slug;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    private NovelStatusEnum status;
    private boolean isClosed;

    @Column(name = "thumbnail_url")
    private String thumbnailImageUrl;

    @Column(name = "read_counts")
    private int readCounts;

    @Column(name = "total_chaps")
    private int totalChapters;

    @Column(name = "avg_rate")
    private BigDecimal averageRatings;

    @Column(name = "like_counts")
    private int likeCounts;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToMany(fetch = LAZY)
    private List<Genre> genres;
}
