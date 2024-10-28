package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.UserReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReadingProgressRepository extends JpaRepository<UserReadingProgress, Integer>{
    @Query("SELECT urp FROM UserReadingProgress urp WHERE urp.readingLibrary.id = ?1")
    UserReadingProgress findByLikedLibraryId(Integer readingLibraryId);
    @Query("SELECT urp FROM UserReadingProgress urp WHERE urp.novel.slug = ?1 AND urp.readingLibrary.id = ?2")
    UserReadingProgress findBySlugAndReadingLibraryId(String slug, Integer readingLibraryId);
    UserReadingProgress findByNovel_SlugAndReadingLibrary_Id(String slug, Integer readingLibraryId);

    List<UserReadingProgress> findAllByReadingLibraryId(Integer readingLibraryId);
}
