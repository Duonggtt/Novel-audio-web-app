package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TierRepository extends JpaRepository<Tier, Long>{
    Tier findByReadCountRequired(int readCountRequired);
}
