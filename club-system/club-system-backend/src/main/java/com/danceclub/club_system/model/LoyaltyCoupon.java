package com.danceclub.club_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 忠誠度優惠券
 * 當會員累積達到 loyaltyThreshold 場已出席活動時由系統自動發放。
 * 會員可在報名時選擇使用（每次報名最多一張）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loyalty_coupon")
public class LoyaltyCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 持有人 userId */
    @NotBlank
    @Column(name = "user_id", nullable = false, length = 10)
    private String userId;

    /** 是否已使用 */
    @NotNull
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /** 發放時間 */
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    /** 使用時間 */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** 用於哪一筆報名 */
    @Column(name = "used_for_registration_id")
    private Long usedForRegistrationId;

    /** 優惠券類型：COUPON / WELCOME / CUSTOM */
    @Column(name = "coupon_type", nullable = true, length = 20)
    private String couponType = "COUPON";

    /** 此券自訂折扣率（null 表示使用當前全域設定；與 discountAmount 擇一） */
    @Column(name = "discount_rate", nullable = true, precision = 4, scale = 2)
    private BigDecimal discountRate;

    /** 固定折扣金額（與 discountRate 擇一；設定此值則忽略 discountRate） */
    @Column(name = "discount_amount", nullable = true, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /** 備注說明 */
    @Column(name = "description", nullable = true, length = 200)
    private String description;

    /** 發放者（"SYSTEM" 表示自動，管理員 userId 表示手動） */
    @Column(name = "issued_by", nullable = true, length = 50)
    private String issuedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.earnedAt == null) this.earnedAt = now;
        if (this.isUsed == null) this.isUsed = false;
        if (this.couponType == null) this.couponType = "COUPON";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
