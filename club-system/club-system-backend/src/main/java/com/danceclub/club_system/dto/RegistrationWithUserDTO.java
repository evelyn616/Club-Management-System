package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationWithUserDTO {
    private Long id;
    private Long activityId;
    private String userId;
    private String userName;
    private RegistrationStatus status;
    private LocalDateTime registrationTime;
    private PaymentStatus paymentStatus;
}
