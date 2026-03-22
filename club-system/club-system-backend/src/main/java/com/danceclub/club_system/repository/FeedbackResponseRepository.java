package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {
    List<FeedbackResponse> findByFormId(Long formId);
    boolean existsByFormIdAndUserId(Long formId, String userId);
    long countByFormId(Long formId);
}
