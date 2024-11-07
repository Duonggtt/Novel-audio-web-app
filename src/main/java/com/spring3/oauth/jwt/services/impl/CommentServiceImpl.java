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
import com.spring3.oauth.jwt.services.NotificationSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final NotificationServiceImpl notificationService;

    @Autowired
    private NotificationSenderService notificationSenderService;

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
        comment.setParent(null);
        commentRepository.save(comment);


        // Kiểm tra và gửi thông báo cho tag
        handleUserTagsInContent(request.getContent(), comment);

        return mapToCommentResponseDTO(comment);
    }

    // Hàm mới để lưu trả lời cho bình luận
    public CommentResponseDTO saveReply(Integer parentCommentId, CreateCommentRequest request) {
        User user = userRepository.findById(Long.valueOf(request.getUserId()))
            .orElseThrow(() -> new RuntimeException("User not found"));

        Comment parentComment = commentRepository.findById(parentCommentId)
            .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setContent(request.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUser(user);
        reply.setNovel(parentComment.getNovel());
        reply.setParent(parentComment); // Trả lời liên kết với bình luận gốc

        commentRepository.save(reply);

        // Kiểm tra và gửi thông báo cho tag
        handleUserTagsInContent(request.getContent(), reply);

        return mapToCommentResponseDTO(reply);
    }

    // Xử lý việc tag người dùng trong nội dung bình luận
    private void handleUserTagsInContent(String content, Comment comment) {
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String taggedUsername = matcher.group(1);
            userRepository.getUserByUsername(taggedUsername).ifPresent(taggedUser -> {
                notificationService.sendTagNotification(taggedUser.getId(), comment.getId(), "Bạn được nhắc đến trong một bình luận.");
                notificationSenderService.sendNotificationToUser(taggedUser.getId(), "Bạn được nhắc đến trong một bình luận.");
            });
        }
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
        commentResponseDTO.setParentId(comment.getParent() != null ? comment.getParent().getId() : null); // Thêm parentId
        commentResponseDTO.setNovelId(comment.getNovel().getId());
        return commentResponseDTO;
    }
}
