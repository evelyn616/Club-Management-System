package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityWithStatsDTO {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private ActivityType activityType;
    private Integer maxParticipants;
    private ActivityStatus activityStatus;
    private BigDecimal feeAmount;
    private LocalDateTime registrationDeadline;
    private Long registrationCount;


}
