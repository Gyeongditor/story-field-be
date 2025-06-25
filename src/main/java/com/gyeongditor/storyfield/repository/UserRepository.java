package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByLoginId(String loginId);
}
