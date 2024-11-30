package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinPackageRepository extends JpaRepository<CoinPackage, String>{
}
