package com.danceclub.club_system.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder // 加上 Builder 會讓 Service 層轉換資料時更好寫
public class LeaveResponseDTO {
    private Long id;
    
    // 申請人資訊
    private String memberId;
    private String memberName;
    
    // 活動資訊
    private Long activityId;
    private String activityTitle; // 多回傳活動名稱，前端會更好呈現
    
    private String leaveType;
    private String reason;
    private String status;
    
    // 審核資訊
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewNote;
    
    private LocalDateTime createdAt;
}