package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.FeedbackQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackQuestionOptionRepository extends JpaRepository<FeedbackQuestionOption, Long> {
    List<FeedbackQuestionOption> findByQuestionIdOrderByOrderIndex(Long questionId);
    void deleteByQuestionId(Long questionId);
}
