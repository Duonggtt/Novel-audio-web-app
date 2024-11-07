package com.spring3.oauth.jwt.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@Slf4j
public class NotificationSenderService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationSenderService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotificationToUser(Long userId, String message) {
        log.info("Sending notification to user with ID {}: {}", userId, message); // Log thông báo gửi
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
    }
}
