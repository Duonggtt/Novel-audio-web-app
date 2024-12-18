package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Integer> {

    Novel findBySlug(String slug);

    @Query("SELECT n " +
            "FROM Novel n " +
            "JOIN n.author a " +
            "JOIN n.genres g " +
            "WHERE n.isClosed = false")
    List<Novel> findAllByClosedFalse();

    @Query("SELECT n " +
            "FROM Novel n " +
            "JOIN n.author a " +
            "JOIN n.genres g " +
            "WHERE n.isClosed = true")
    List<Novel> findAllByClosedTrue();

    // query theo xu hướng với phân trang
    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE n.isClosed = false " +
        "ORDER BY n.likeCounts DESC")
    Page<Novel> findAllByClosedFalseOrderByLikeCountsDesc(Pageable pageable);

    // query theo top với phân trang
    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE n.isClosed = false " +
        "ORDER BY n.readCounts DESC")
    Page<Novel> findAllByClosedFalseOrderByReadCountsDesc(Pageable pageable);

    @Query("SELECT DISTINCT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE n.releasedAt >= :sevenDaysAgo " +
        "AND n.isClosed = false " +
        "ORDER BY n.releasedAt DESC")
    Page<Novel> findAllByReleasedAtWithinLast7Days(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
                                                   Pageable pageable);


    @Query("SELECT NovelResponseDTO(" +
            "n.title, " +
            "n.description, " +
            "n.releasedAt, " +
            "n.status, " +
            "n.isClosed, " +
            "n.thumbnailImageUrl, " +
            "n.readCounts, " +
            "n.totalChapters, " +
            "n.averageRatings, " +
            "n.likeCounts, " +
            "a.fullName, " +
            "g.name) " +
            "FROM Novel n " +
            "JOIN n.author a " +
            "JOIN n.genres g " +
            "WHERE n.id = ?1")
    Optional<NovelResponseDTO> findNovelDtoById(Integer id);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE g.id IN ?1")
    Page<Novel> findAllByGenres_IdIn(List<Integer> genreIds, Pageable pageable);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE g.name LIKE %?1%")
    Page<Novel> findAllByGenreName(String genreName, Pageable pageable);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE a.id = ?1")
    Page<Novel> findAllByAuthor_Id(Integer authorId, Pageable pageable);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE n.isClosed = false AND a.fullName LIKE %?1%")
    Page<Novel> findAllByAuthorName(String authorName, Pageable pageable);

    @Query("SELECT n " +
            "FROM Novel n " +
            "JOIN n.author a " +
            "JOIN n.genres g " +
            "WHERE a.username LIKE %?1%")
    Page<Novel> findAllByAuthorAuthName(String username, Pageable pageable);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE n.title LIKE %?1%")
    Page<Novel> findAllByTitleContaining(String title, Pageable pageable);

    Novel findByTitle(String title);

    @Query("SELECT COUNT(n) " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE g.id = ?1")
    int countByGenres(Integer genreIds);

    @Query("SELECT DATE(n.releasedAt) as date, SUM(n.likeCounts) as likeCounts " +
        "FROM Novel n " +
        "WHERE n.releasedAt >= :startDate " +
        "GROUP BY DATE(n.releasedAt) " +
        "ORDER BY DATE(n.releasedAt)")
    List<Object[]> findLikeCountsByDay(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(n) " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g")
    int countAll();

}
