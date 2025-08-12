package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByMailVerificationToken(String token);

    Optional<User> findBySocialId(String socialId);
}
