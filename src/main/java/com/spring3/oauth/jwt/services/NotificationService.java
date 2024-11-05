package com.spring3.oauth.jwt.services;

import com.spring3.oauth.jwt.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendTagNotification(long taggedUserId, Integer commentId, String message);//Tạo một thông báo mới khi người dùng được tag.
    List<Notification> getUnreadNotifications(long userId);//Lấy danh sách thông báo chưa đọc của một người dùng.
    void markAsRead(Integer notificationId);//danh dau la da doc
    List<Notification> getNotifications(long userId);//Lấy danh sách tất cả thông báo của một người dùng.
}
