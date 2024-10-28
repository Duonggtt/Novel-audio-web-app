package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Integer> {
    @Query("SELECT c FROM Chapter c WHERE c.novel.slug = :slug")
    List<Chapter> findAllByNovelSlug(String slug);
    Chapter findByChapterNoAndNovelSlug(int chapNo, String slug);
}
