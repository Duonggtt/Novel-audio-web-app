package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Notification;
import com.spring3.oauth.jwt.models.dtos.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    void sendTagNotification(long taggedUserId, Integer commentId, String message);//Tạo một thông báo mới khi người dùng được tag.
    List<NotificationResponseDTO> getUnreadNotifications(long userId);//Lấy danh sách thông báo chưa đọc của một người dùng.
    void markAsRead(Integer notificationId);//danh dau la da doc
    List<NotificationResponseDTO> getNotifications(long userId);//Lấy danh sách tất cả thông báo của một người dùng.
}
