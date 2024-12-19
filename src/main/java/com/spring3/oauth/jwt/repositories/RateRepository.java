package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Rate;
import com.spring3.oauth.jwt.models.dtos.RateListResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateRepository extends JpaRepository<Rate, Integer> {
    Rate findByNovelId(Integer novelId);
    Rate findByNovel_Slug(String slug);

    @Query("SELECT new com.spring3.oauth.jwt.models.dtos.RateListResponseDTO(r.id, r.user.fullName, r.novel.title, r.ratedAt, r.ratePoint) FROM UserRate r")
    List<RateListResponseDTO> findAllRatePoint();
}
