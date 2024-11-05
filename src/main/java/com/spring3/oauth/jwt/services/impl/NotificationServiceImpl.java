package com.spring3.oauth.jwt.services.impl;

import com.spring3.oauth.jwt.entity.Comment;
import com.spring3.oauth.jwt.entity.Notification;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.exception.NotFoundException;
import com.spring3.oauth.jwt.repositories.CommentRepository;
import com.spring3.oauth.jwt.repositories.NotificationRepository;
import com.spring3.oauth.jwt.repositories.UserRepository;
import com.spring3.oauth.jwt.services.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public void sendTagNotification(long taggedUserId, Integer commentId, String message) {

        User user = userRepository.findById(taggedUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setComment(comment);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false); // Mặc định là chưa đọc

        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
