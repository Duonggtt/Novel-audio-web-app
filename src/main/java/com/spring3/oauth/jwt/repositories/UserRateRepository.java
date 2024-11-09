package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.UserRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRateRepository extends JpaRepository<UserRate, Integer> {
    Optional<UserRate> findByUser_IdAndNovel_Id(Long userId, Integer novelId);
    Optional<UserRate> findByUser_IdAndNovel_Slug(Long userId, String slug);
    boolean existsByUser_IdAndNovel_Id(Long userId, Integer novelId);
}
