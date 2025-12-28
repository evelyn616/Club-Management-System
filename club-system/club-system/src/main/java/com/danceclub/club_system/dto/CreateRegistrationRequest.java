package com.danceclub.club_system.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立報名的請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrationRequest {

    @NotNull(message = "活動ID不得為空")
    private Long activityId;

    @NotBlank(message = "會員ID不得為零")
    private String userId;
}
