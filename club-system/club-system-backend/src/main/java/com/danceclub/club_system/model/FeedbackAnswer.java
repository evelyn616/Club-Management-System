package com.danceclub.club_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "feedback_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "response_id", nullable = false)
    private Long responseId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer; // TEXT 題型

    @Column(name = "rating_answer")
    private Integer ratingAnswer; // RATING 題型

    @Column(name = "selected_option_ids", length = 500)
    private String selectedOptionIds; // 逗號分隔 option id，SINGLE/MULTIPLE 題型
}
