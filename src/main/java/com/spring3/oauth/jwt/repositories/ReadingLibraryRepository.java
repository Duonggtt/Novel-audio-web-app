package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.ReadingLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingLibraryRepository extends JpaRepository<ReadingLibrary, Integer>{
    ReadingLibrary findByUser_Id(long userId);
}
