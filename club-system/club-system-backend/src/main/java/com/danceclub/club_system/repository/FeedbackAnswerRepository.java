package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.FeedbackAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {
    List<FeedbackAnswer> findByResponseId(Long responseId);
    List<FeedbackAnswer> findByQuestionId(Long questionId);
}
