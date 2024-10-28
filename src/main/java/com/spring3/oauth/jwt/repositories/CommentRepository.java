package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer>{
    List<Comment> findAllByNovelSlug(String slug);
}
