package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.LikedLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LikedLibraryRepository extends JpaRepository<LikedLibrary, Integer> {
    @Query("SELECT l FROM LikedLibrary l WHERE l.user.id = :userId")
    LikedLibrary findByUser_Id(Integer userId);
}
