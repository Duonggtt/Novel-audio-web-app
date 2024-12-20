package com.spring3.oauth.jwt.controllers;

import com.spring3.oauth.jwt.entity.Notification;
import com.spring3.oauth.jwt.models.dtos.NotificationResponseDTO;
import com.spring3.oauth.jwt.services.NotificationSenderService;
import com.spring3.oauth.jwt.services.NotificationService;
import com.spring3.oauth.jwt.services.impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3388", "https://80ba-14-231-167-47.ngrok-free.app"})
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationServiceImpl notificationService;
    private final NotificationSenderService notificationSenderService;

    @GetMapping("/")
    public ResponseEntity<?> getNotifications(@RequestParam long userId) {
        List<NotificationResponseDTO> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // Lấy thông báo chưa đọc của người dùng
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam long userId) {
        List<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // Đánh dấu một thông báo là đã đọc
    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markAsRead(@RequestParam Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/send-notification")
    public ResponseEntity<?> sendRealTimeNotification(@RequestParam long userId, @RequestParam String message) {
        notificationSenderService.sendNotificationToUser(userId, message);
        return ResponseEntity.ok("Notification sent in real-time");
    }
}
