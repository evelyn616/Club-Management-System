package com.danceclub.club_system.dto;

import java.util.List;

/**
 * 提交回饋表單的請求 DTO
 */
public class FeedbackSubmitRequest {

    private List<AnswerRequest> answers;

    public static class AnswerRequest {
        private Long questionId;
        private String textAnswer;       // TEXT 題型
        private Integer ratingAnswer;    // RATING 題型
        private List<Long> selectedOptionIds; // SINGLE/MULTIPLE 題型

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
        public Integer getRatingAnswer() { return ratingAnswer; }
        public void setRatingAnswer(Integer ratingAnswer) { this.ratingAnswer = ratingAnswer; }
        public List<Long> getSelectedOptionIds() { return selectedOptionIds; }
        public void setSelectedOptionIds(List<Long> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
    }

    public List<AnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<AnswerRequest> answers) { this.answers = answers; }
}
