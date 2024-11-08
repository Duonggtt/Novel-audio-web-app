package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>{
    List<Notification> findByUserIdAndIsReadFalse(long userId); // Lấy thông báo chưa đọc
    List<Notification> findByUserId(long userId); // Lấy tất cả thông báo của user
    List<Notification> findAllByComment_Id(Integer commentId); // Lấy tất cả thông báo của một comment@Modifying
}
