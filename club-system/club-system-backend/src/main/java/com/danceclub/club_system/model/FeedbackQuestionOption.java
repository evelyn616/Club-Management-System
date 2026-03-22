package com.danceclub.club_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "feedback_question_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_text", nullable = false, length = 300)
    private String optionText;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
