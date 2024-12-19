package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Tier;
import com.spring3.oauth.jwt.helpers.RefreshableCRUDRepository;
import com.spring3.oauth.jwt.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends RefreshableCRUDRepository<User, Long> {

   public User findByUsername(String username);
   User findFirstById(Long id);
   User findByEmail(String email);
   List<User> findAllByUsername(String username);
   List<User> findAllByEmail(String email);
   Optional<User> getUserByUsername(String username);
   @Query("SELECT u FROM User u ORDER BY u.chapterReadCount DESC LIMIT 5")
   List<User> findFifthUsersReadingTheMost();
   @Query("SELECT u FROM User u ORDER BY u.point DESC LIMIT 4")
   List<User> findFourUsersHaveHighestScore();
   @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.id = ?2")
   User findByUsernameAndId(String username, long id);
   User getUserById(long id);
   @Query("SELECT COUNT(u) FROM User u WHERE u.tier.id = ?1")
   int countAllByTierId(long tier);

   @Query(nativeQuery = true, value = """
        SELECT 
            CASE 
                WHEN point BETWEEN 0 AND 99 THEN '0-99'
                WHEN point BETWEEN 100 AND 499 THEN '100-499'
                WHEN point BETWEEN 500 AND 999 THEN '500-999'
                WHEN point BETWEEN 1000 AND 4999 THEN '1000-4999'
                WHEN point >= 5000 THEN '5000+'
            END AS score_range,
            COUNT(*) as user_count
        FROM users
        GROUP BY score_range
        ORDER BY 
            CASE score_range
                WHEN '0-99' THEN 1
                WHEN '100-499' THEN 2
                WHEN '500-999' THEN 3
                WHEN '1000-4999' THEN 4
                WHEN '5000+' THEN 5
            END
    """)
   List<Object[]> getUserCountByScoreRange();

   @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt < :date")
   long countByCreatedDateBefore(LocalDateTime date);

   @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
   long countByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

   @Query("SELECT SUM(u.chapterReadCount) FROM User u")
   long countAllReadCounts();
}
