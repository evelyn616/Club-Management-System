package com.danceclub.club_system.model;

import com.danceclub.club_system.model.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "feedback_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "max_rating")
    private Integer maxRating = 10;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;
}
