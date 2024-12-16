package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.User;
import com.spring3.oauth.jwt.entity.enums.ChatRoomTypeEnum;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByAuthorId(Long authorId);
    List<ChatRoom> findByParticipantsContaining(User participant);

    @Query("""
    SELECT r FROM ChatRoom r
    WHERE r.roomType = :publicType
    OR (r.roomType = :privateType AND r.author.id IN (
        SELECT uf.id FROM User uf JOIN uf.followers f WHERE f.id = :userId
    ))
    """)
    List<ChatRoom> findAvailableChatRooms(
            @Param("publicType") ChatRoomTypeEnum publicType,
            @Param("privateType") ChatRoomTypeEnum privateType,
            @Param("userId") Long userId);
}