package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.helpers.RefreshableCRUDRepository;
import com.spring3.oauth.jwt.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends RefreshableCRUDRepository<User, Long> {

   public User findByUsername(String username);
   User findFirstById(Long id);
   User findByEmail(String email);
   List<User> findAllByUsername(String username);
   List<User> findAllByEmail(String email);
   Optional<User> getUserByUsername(String username);
   @Query("SELECT u FROM User u ORDER BY u.chapterReadCount DESC LIMIT 5")
   List<User> findFifthUsersReadingTheMost();
   @Query("SELECT u FROM User u ORDER BY u.point DESC LIMIT 4")
   List<User> findFourUsersHaveHighestScore();
   @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.id = ?2")
   User findByUsernameAndId(String username, long id);
   User getUserById(long id);
}
