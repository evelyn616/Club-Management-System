package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
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

    public static RegistrationWithUserDTO from(Registration registration, String userName){
        return RegistrationWithUserDTO.builder()
                .id(registration.getId())
                .activityId(registration.getActivityId())
                .userId(registration.getUserId())
                .userName(userName)
                .status(registration.getStatus())
                .registrationTime(registration.getRegistrationTime())
                .paymentStatus(registration.getPaymentStatus())
                .build();
    }
}
