package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.Activity;
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
public class ActivityRegistrationSummaryDTO {

    // 活動資訊
    private Long activityId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private BigDecimal feeAmount;

    // 報名統計
    private Long totalRegistrations;      // 總報名數
    private Long paidCount;               // 已繳費
    private Long pendingPaymentCount;     // 待繳費
    private Long checkedInCount;          // 已簽到
    private Long cancelledCount;          // 已取消

    // 計算欄位
    private Double registrationRate;      // 報名率（報名數/人數上限）
    private Double paymentRate;           // 繳費率（已繳費/總報名）
    private Double checkInRate;           // 簽到率（已簽到/總報名）

    public static ActivityRegistrationSummaryDTO from(Activity activity,
                                                      Long totalRegistrations,
                                                      Long paidCount,
                                                      Long pendingPaymentCount,
                                                      Long checkedInCount,
                                                      Long cancelledCount) {
        // 計算比率
        double registrationRate = activity.getMaxParticipants() != null &&
                activity.getMaxParticipants() > 0
                ? (double) totalRegistrations / activity.getMaxParticipants() * 100
                : 0.0;

        double paymentRate = totalRegistrations > 0
                ? (double) paidCount / totalRegistrations * 100
                : 0.0;

        double checkInRate = totalRegistrations > 0
                ? (double) checkedInCount / totalRegistrations * 100
                : 0.0;

        return ActivityRegistrationSummaryDTO.builder()
                .activityId(activity.getId())
                .title(activity.getTitle())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .maxParticipants(activity.getMaxParticipants())
                .feeAmount(activity.getFeeAmount())
                .totalRegistrations(totalRegistrations)
                .paidCount(paidCount)
                .pendingPaymentCount(pendingPaymentCount)
                .checkedInCount(checkedInCount)
                .cancelledCount(cancelledCount)
                .registrationRate(registrationRate)
                .paymentRate(paymentRate)
                .checkInRate(checkInRate)
                .build();
    }
}
