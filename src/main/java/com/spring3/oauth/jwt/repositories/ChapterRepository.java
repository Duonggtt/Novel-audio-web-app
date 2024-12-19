package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    @Query("SELECT c FROM Chapter c WHERE c.novel.slug = :slug")
    List<Chapter> findAllByNovelSlug(String slug);
    Chapter findByChapterNoAndNovelSlug(int chapNo, String slug);
    @Query("SELECT c FROM Chapter c WHERE c.novel.slug = :slug ORDER BY c.chapterNo ASC")
    Page<Chapter> findAllByNovelSlugPage(String slug, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.releasedAt < :date")
    long countByCreatedDateBefore(LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.releasedAt BETWEEN :startDate AND :endDate")
    long countByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
