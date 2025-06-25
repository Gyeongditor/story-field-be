package com.gyeongditor.storyfield.repository;

import com.gyeongditor.storyfield.Entity.StoryPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryPageRepository extends JpaRepository<StoryPage, String> {
    List<StoryPage> findByStoryIdOrderByStoryPageNumAsc(String storyId);
    Optional<StoryPage> findFirstByStoryIdAndStoryPageNum(String storyId, int storyPageNum);

}
