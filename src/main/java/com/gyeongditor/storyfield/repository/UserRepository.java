package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
