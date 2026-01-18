package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private ActivityType activityType;
    private BigDecimal feeAmount;
    private ActivityStatus status;

    //預約發布時間
    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityResponse from(Activity activity){
        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .activityType(activity.getActivityType())
                .feeAmount(activity.getFeeAmount())
                .status(activity.getStatus())
                .publishedAt(activity.getPublishedAt())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}


