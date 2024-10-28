package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.helpers.RefreshableCRUDRepository;
import com.spring3.oauth.jwt.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends RefreshableCRUDRepository<User, Long> {

   public User findByUsername(String username);
   User findFirstById(Long id);
   User findByEmail(String email);
   List<User> findAllByUsername(String username);
   List<User> findAllByEmail(String email);

}
