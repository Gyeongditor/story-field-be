package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {
}
