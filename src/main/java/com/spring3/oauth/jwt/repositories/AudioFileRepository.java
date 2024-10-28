package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Integer>{
    AudioFile findByChapterId(Integer chapterId);
}
