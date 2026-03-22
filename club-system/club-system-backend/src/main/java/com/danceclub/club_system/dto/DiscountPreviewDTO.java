package com.danceclub.club_system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 報名前折扣預覽 DTO
 */
@Data
@NoArgsConstructor
public class DiscountPreviewDTO {

    private BigDecimal originalAmount;
    private boolean officerFree;
    private boolean earlyBirdActive;
    private LocalDateTime earlyBirdDeadline;
    private BigDecimal earlyBirdRate;
    private BigDecimal earlyBirdAmount;
    private List<LoyaltyCouponDTO> availableCoupons;
    private BigDecimal loyaltyRate;
    private BigDecimal loyaltyAmount;

    @Data
    @NoArgsConstructor
    public static class LoyaltyCouponDTO {
        private Long id;
        private LocalDateTime earnedAt;
        private BigDecimal discountRate;
        private String description;
        private String couponType;
        private BigDecimal discountAmount;

        public LoyaltyCouponDTO(Long id, LocalDateTime earnedAt, BigDecimal discountRate,
                                String description, String couponType, BigDecimal discountAmount) {
            this.id = id;
            this.earnedAt = earnedAt;
            this.discountRate = discountRate;
            this.description = description;
            this.couponType = couponType;
            this.discountAmount = discountAmount;
        }
    }
}
