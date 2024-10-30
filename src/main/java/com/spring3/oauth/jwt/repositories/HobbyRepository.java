package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HobbyRepository extends JpaRepository<Hobby, Integer> {
    Hobby findByName(String name);
}
