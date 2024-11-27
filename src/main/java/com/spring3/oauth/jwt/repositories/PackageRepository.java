package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long>{
}
