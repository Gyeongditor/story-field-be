package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {
    public boolean existsById(String storyId);
}
