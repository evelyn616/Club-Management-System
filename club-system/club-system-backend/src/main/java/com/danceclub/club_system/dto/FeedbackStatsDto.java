package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.QuestionType;

import java.util.List;
import java.util.Map;

/**
 * 回饋表單統計摘要 DTO
 */
public class FeedbackStatsDto {

    private Long formId;
    private String formTitle;
    private Long totalResponses;
    private List<QuestionStats> questions;

    public static class QuestionStats {
        private Long questionId;
        private String questionText;
        private QuestionType questionType;
        private Long answeredCount; // 有作答人數

        // RATING 專用
        private Double averageRating;
        private Map<Integer, Long> ratingDistribution; // 分數 -> 人數

        // SINGLE/MULTIPLE 專用
        private List<OptionCount> optionCounts;

        // TEXT 專用
        private List<String> textAnswers;

        // Getters & Setters
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
        public Long getAnsweredCount() { return answeredCount; }
        public void setAnsweredCount(Long answeredCount) { this.answeredCount = answeredCount; }
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
        public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
        public List<OptionCount> getOptionCounts() { return optionCounts; }
        public void setOptionCounts(List<OptionCount> optionCounts) { this.optionCounts = optionCounts; }
        public List<String> getTextAnswers() { return textAnswers; }
        public void setTextAnswers(List<String> textAnswers) { this.textAnswers = textAnswers; }
    }

    public static class OptionCount {
        private Long optionId;
        private String optionText;
        private Long count;
        private Double percentage;

        public Long getOptionId() { return optionId; }
        public void setOptionId(Long optionId) { this.optionId = optionId; }
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    // Getters & Setters
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }
    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }
    public Long getTotalResponses() { return totalResponses; }
    public void setTotalResponses(Long totalResponses) { this.totalResponses = totalResponses; }
    public List<QuestionStats> getQuestions() { return questions; }
    public void setQuestions(List<QuestionStats> questions) { this.questions = questions; }
}
