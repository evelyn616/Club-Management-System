package com.danceclub.club_system.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * QR Code 掃碼簽到請求 DTO
 * POST /api/registrations/checkin-by-user
 */
public class CheckInRequest {
    @NotBlank(message = "userId 不可為空")
    private String userId;

    @NotNull(message = "activityId 不可為空")
    private Long activityId;

    public CheckInRequest() {}

    public CheckInRequest(String userId, Long activityId) {
        this.userId = userId;
        this.activityId = activityId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
}






