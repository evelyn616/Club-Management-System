package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentMethod;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;
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
public class RegistrationDetailDTO {

    //報名基本資訊
    private Long id;
    private Long activityId;
    private String userId;
    private RegistrationStatus status;
    private LocalDateTime registrationTime;
    private Boolean checkedIn;
    private LocalDateTime checkedInTime;
    private String note;

    //會員資訊
    private String userName;
    private String userEmail;
    private String userPhone;

    //活動資訊
    private String activityTitle;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private String activityLocation;
    private BigDecimal activityFeeAmount;

    //繳費資訊
    private PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDeadline;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;

    //靜態方法
    public static RegistrationDetailDTO from(Registration registration, UserResponse user, Activity activity, Payment payment){
        RegistrationDetailDTOBuilder builder = RegistrationDetailDTO.builder()
                // 報名資訊
                .id(registration.getId())
                .activityId(registration.getActivityId())
                .userId(registration.getUserId())
                .status(registration.getStatus())
                .registrationTime(registration.getRegistrationTime())
                .checkedIn(registration.getCheckedIn())
                .checkedInTime(registration.getCheckInTime())
                .note(registration.getNote())

                // 會員資訊
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userPhone(user.getPhone())

                // 活動資訊
                .activityTitle(activity.getTitle())
                .activityStartTime(activity.getStartTime())
                .activityEndTime(activity.getEndTime())
                .activityLocation(activity.getLocation())
                .activityFeeAmount(activity.getFeeAmount());

        // 繳費資訊（可能為 null）
        if (payment != null) {
            builder.paymentStatus(payment.getStatus())
                    .paymentAmount(payment.getAmount())
                    .paymentDeadline(payment.getPaymentDeadline())
                    .paymentMethod(payment.getMethod())
                    .paidAt(payment.getPaidAt());
        }

        return builder.build();
    }
}
