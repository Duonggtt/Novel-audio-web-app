package com.spring3.oauth.jwt.repositories;


import com.spring3.oauth.jwt.entity.ChatRoom;
import com.spring3.oauth.jwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByAuthorId(Long authorId);
    List<ChatRoom> findByParticipantsContaining(User participant);

}