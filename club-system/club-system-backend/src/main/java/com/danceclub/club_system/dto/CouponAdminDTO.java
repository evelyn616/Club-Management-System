package com.danceclub.club_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理員查看優惠券列表用 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponAdminDTO {
    private Long id;
    private String userId;
    private String userName;
    private String couponType;   // "COUPON" | "WELCOME" | "CUSTOM"
    private BigDecimal discountRate;
    private String description;
    private Boolean isUsed;
    private LocalDateTime earnedAt;
    private LocalDateTime usedAt;
    private String issuedBy;
}
