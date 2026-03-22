package com.danceclub.club_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 優惠碼
 * discountType: "FIXED_AMOUNT"（折扣金額）| "PERCENTAGE"（折扣比例 0.01~1.0）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promo_code")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 優惠碼（唯一，自動轉大寫） */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /** "FIXED_AMOUNT" 固定金額折扣 | "PERCENTAGE" 比例折扣（0.01~1.0） */
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    /** 折扣值：FIXED_AMOUNT 時為金額（如 100），PERCENTAGE 時為比例（如 0.8） */
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    /** 說明 */
    @Column(nullable = true, length = 200)
    private String description;

    /** 是否啟用 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 最多使用次數（null = 無限制） */
    @Column(name = "max_usage", nullable = true)
    private Integer maxUsage;

    /** 已使用次數 */
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    /** 到期時間（null = 永不過期） */
    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt;

    /** 建立者 */
    @Column(name = "created_by", nullable = true, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isActive == null) this.isActive = true;
        if (this.usedCount == null) this.usedCount = 0;
        if (this.code != null) this.code = this.code.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
