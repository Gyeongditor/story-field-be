package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {
    Page<Story> findAll(Pageable pageable);

}
