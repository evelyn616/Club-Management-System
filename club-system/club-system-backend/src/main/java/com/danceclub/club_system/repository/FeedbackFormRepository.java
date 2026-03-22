package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {
    Optional<FeedbackForm> findByActivityId(Long activityId);
    boolean existsByActivityId(Long activityId);
}
