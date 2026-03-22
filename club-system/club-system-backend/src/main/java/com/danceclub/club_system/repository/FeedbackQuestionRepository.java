package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.FeedbackQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackQuestionRepository extends JpaRepository<FeedbackQuestion, Long> {
    List<FeedbackQuestion> findByFormIdOrderByOrderIndex(Long formId);
    void deleteByFormId(Long formId);
}
