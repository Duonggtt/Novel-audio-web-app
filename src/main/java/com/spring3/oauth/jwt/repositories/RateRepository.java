package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<Rate, Integer> {
    Rate findByNovelId(Integer novelId);
    Rate findByNovel_Slug(String slug);
}
