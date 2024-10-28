package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Comment;
import com.spring3.oauth.jwt.entity.Novel;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.models.dtos.CommentResponseDTO;
import com.spring3.oauth.jwt.models.request.CreateCommentRequest;
import com.spring3.oauth.jwt.repositories.CommentRepository;
import com.spring3.oauth.jwt.repositories.NovelRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    @Override
    public CommentResponseDTO saveComment(CreateCommentRequest request) {

        User user = userRepository.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Novel novel = novelRepository.findBySlug(request.getSlug());
        if (novel == null) {
            throw new RuntimeException("Novel not found");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(user);
        comment.setNovel(novel);
        commentRepository.save(comment);
        return mapToCommentResponseDTO(comment);
    }

    @Override
    public void deleteComment(Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentResponseDTO> getAllCommentsInNovel(String slug) {
        return commentRepository.findAllByNovelSlug(slug).stream()
                .map(this::mapToCommentResponseDTO)
                .toList();
    }

    CommentResponseDTO mapToCommentResponseDTO(Comment comment) {

        CommentResponseDTO commentResponseDTO = new CommentResponseDTO();
        commentResponseDTO.setId(comment.getId());
        commentResponseDTO.setContent(comment.getContent());
        commentResponseDTO.setCreatedAt(comment.getCreatedAt());
        commentResponseDTO.setUserId((int) comment.getUser().getId());
        commentResponseDTO.setUsername(comment.getUser().getUsername());
        commentResponseDTO.setUser_image_path(comment.getUser().getImagePath());
        commentResponseDTO.setNovelId(comment.getNovel().getId());
        return commentResponseDTO;
    }
}
