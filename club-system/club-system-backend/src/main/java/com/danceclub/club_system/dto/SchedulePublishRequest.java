package com.danceclub.club_system.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchedulePublishRequest
{
    @Future(message = "發布時間必須是未來時間")
    private LocalDateTime publishAt;
}
