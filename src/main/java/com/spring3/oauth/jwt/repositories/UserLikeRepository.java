package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUser_IdAndNovel_Slug(long userId, String novelSlug);

    @Query("SELECT ul.novel.slug FROM UserLike ul WHERE ul.user.id = :userId")
    List<String> findLikedNovelSlugsByUser(@Param("userId") Long userId);

    @Query("SELECT ul.novel.slug FROM UserLike ul WHERE ul.user.id = :userId AND ul.novel.id IN :novelIds")
    List<String> findLikedNovels(@Param("userId") Long userId, @Param("novelIds") List<Integer> novelIds);


    @Query("SELECT DATE(ul.likedAt) as date, COUNT(ul.id) as likeCounts " +
        "FROM UserLike ul " +
        "WHERE ul.likedAt >= :startDate " +
        "GROUP BY DATE(ul.likedAt) " +
        "ORDER BY DATE(ul.likedAt)")
    List<Object[]> findTotalLikeCountsByDay(@Param("startDate") LocalDateTime startDate);

}
