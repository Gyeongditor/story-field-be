package com.gyeongditor.storyfield.Repository;

import com.gyeongditor.storyfield.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByLoginId(String loginId);
}
