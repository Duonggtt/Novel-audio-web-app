package com.spring3.oauth.jwt.repositories;

import com.spring3.oauth.jwt.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    Set<Role> findAllByName(String name);
}
