package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.ActivityType;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
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
public class UserRegistrationDTO {

    // 報名資訊
    private Long registrationId;
    private RegistrationStatus status;
    private LocalDateTime registrationTime;
    private Boolean checkedIn;

    // 活動摘要
    private Long activityId;
    private String activityTitle;
    private ActivityType activityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;

    // 繳費資訊
    private PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDeadline;
    private Boolean canCancel;  // 是否可取消報名

    public static UserRegistrationDTO from(Registration registration,
                                           Activity activity,
                                           Payment payment) {
        UserRegistrationDTOBuilder builder = UserRegistrationDTO.builder()
                // 報名資訊
                .registrationId(registration.getId())
                .status(registration.getStatus())
                .registrationTime(registration.getRegistrationTime())
                .checkedIn(registration.getCheckedIn())

                // 活動摘要
                .activityId(activity.getId())
                .activityTitle(activity.getTitle())
                .activityType(activity.getActivityType())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())

                // 繳費資訊
                .paymentStatus(registration.getPaymentStatus())
                .paymentAmount(registration.getPaymentAmount());

        // 繳費期限（可能為 null）
        if (payment != null) {
            builder.paymentDeadline(payment.getPaymentDeadline());
        }

        // 判斷是否可取消
        boolean canCancel = registration.canCancel() &&
                LocalDateTime.now().isBefore(activity.getStartTime());
        builder.canCancel(canCancel);

        return builder.build();
    }
}
