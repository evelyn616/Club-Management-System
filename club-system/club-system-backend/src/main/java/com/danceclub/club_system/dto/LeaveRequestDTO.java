package com.danceclub.club_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private String memberId;    // 對應 User 的 String id (例如: m0001)
    private Long activityId;    // 活動 ID
    private String leaveType;   // 事假、病假等
    private String reason;      // 請假原因
}