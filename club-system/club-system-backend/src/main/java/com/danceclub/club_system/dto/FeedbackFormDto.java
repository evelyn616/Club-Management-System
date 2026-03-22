package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 表單完整結構（含題目與選項），供前端填寫頁與管理頁使用
 */
public class FeedbackFormDto {

    private Long id;
    private Long activityId;
    private String activityTitle; // 補充顯示用
    private String title;
    private String description;
    private Boolean isOpen;
    private Integer pointsReward;
    private Long responseCount;
    private Boolean hasSubmitted; // 當前使用者是否已提交（登入時才有值）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionDto> questions;

    public static class QuestionDto {
        private Long id;
        private String questionText;
        private QuestionType questionType;
        private Boolean isRequired;
        private Integer maxRating;
        private Integer orderIndex;
        private List<OptionDto> options;

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
        public Boolean getIsRequired() { return isRequired; }
        public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
        public Integer getMaxRating() { return maxRating; }
        public void setMaxRating(Integer maxRating) { this.maxRating = maxRating; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public List<OptionDto> getOptions() { return options; }
        public void setOptions(List<OptionDto> options) { this.options = options; }
    }

    public static class OptionDto {
        private Long id;
        private String optionText;
        private Integer orderIndex;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
    public Integer getPointsReward() { return pointsReward; }
    public void setPointsReward(Integer pointsReward) { this.pointsReward = pointsReward; }
    public Long getResponseCount() { return responseCount; }
    public void setResponseCount(Long responseCount) { this.responseCount = responseCount; }
    public Boolean getHasSubmitted() { return hasSubmitted; }
    public void setHasSubmitted(Boolean hasSubmitted) { this.hasSubmitted = hasSubmitted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
}
