package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.enums.NovelStatusEnum;
import com.spring3.oauth.jwt.models.dtos.NovelResponseDTO;
import com.spring3.oauth.jwt.repositories.itf.NovelStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    List<Novel> findAllByAuthorAuthName(String username);
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

    @Query("SELECT COUNT(n) FROM Novel n WHERE n.releasedAt < :date")
    long countByCreatedDateBefore(LocalDateTime date);

    @Query("SELECT COUNT(n) FROM Novel n WHERE n.releasedAt BETWEEN :startDate AND :endDate")
    long countByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT n " +
        "FROM Novel n " +
        "JOIN n.author a " +
        "JOIN n.genres g " +
        "WHERE g.id = ?1")
    List<Novel> findAllByGenreId(Integer id);

    @Query("""
            SELECT n FROM Novel n 
            WHERE n.releasedAt BETWEEN :startDate AND :endDate 
            OR n.status = :status""")
    List<Novel> findNovelsForStats(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("status") NovelStatusEnum status
    );

    @Query(value = """
        SELECT 
            DATE(n.released_at) AS date,
            COALESCE(COUNT(CASE WHEN n.status = 'COMPLETED' THEN 1 END), 0) AS completed_novels,
            COUNT(n.id) AS new_novels
        FROM novels n
        WHERE n.released_at >= :startDate AND n.released_at < :endDate + INTERVAL 1 DAY
        GROUP BY DATE(n.released_at)
        ORDER BY DATE(n.released_at);
        """, nativeQuery = true)
    List<NovelStatsProjection> getDailyStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT 
            DATE_FORMAT(n.released_at, '%Y-%m') AS date,
            COALESCE(COUNT(CASE WHEN n.status = 'COMPLETED' THEN 1 END), 0) AS completed_novels,
            COUNT(n.id) AS new_novels
        FROM novels n
        WHERE n.released_at >= :startDate AND n.released_at < :endDate + INTERVAL 1 DAY
        GROUP BY DATE_FORMAT(n.released_at, '%Y-%m')
        ORDER BY DATE_FORMAT(n.released_at, '%Y-%m');
        """, nativeQuery = true)
    List<NovelStatsProjection> getMonthlyStats(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );




}
