package com.danceclub.club_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
//用於儀表板
public class RegistrationStatsDTO {
    // 總體統計
    private Long totalRegistrations;
    private Long todayRegistrations;
    private Long thisWeekRegistrations;
    private Long thisMonthRegistrations;

    // 繳費統計
    private Long pendingPaymentCount;
    private BigDecimal pendingPaymentTotal;
    private Long paidCount;
    private BigDecimal paidTotal;

    // 活動統計
    private Long upcomingActivitiesCount;
    private Long nearlyFullActivitiesCount;

    // 簽到統計
    private Long todayCheckInCount;
    private Long todayActivitiesCount;
}