package com.spring3.oauth.jwt.services;


import com.spring3.oauth.jwt.models.dtos.CommentResponseDTO;
import com.spring3.oauth.jwt.models.request.CreateCommentRequest;

import java.util.List;

public interface CommentService {
    CommentResponseDTO saveComment(CreateCommentRequest request);
    void deleteComment(Integer id);
    List<CommentResponseDTO> getAllCommentsInNovel(String slug);
}
