package com.danceclub.club_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 折扣全域設定（系統只會有一筆，id 固定為 1）
 *
 * earlyBirdDefaultDays  - 早鳥截止：報名開始後幾天（預設 3）
 * earlyBirdRate         - 早鳥折扣率（0.0~1.0，例如 0.8 = 八折）
 * loyaltyThreshold      - 累積幾場活動可獲得優惠券（預設 5）
 * loyaltyRate           - 忠誠優惠券折扣率（預設 0.5 = 半價）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_config")
public class DiscountConfig {

    @Id
    @Column(name = "id")
    private Long id = 1L;

    /** 早鳥期限：發布後幾天內報名享早鳥優惠 */
    @NotNull
    @Min(value = 1, message = "早鳥天數至少為1天")
    @Column(name = "early_bird_default_days", nullable = false)
    private Integer earlyBirdDefaultDays = 3;

    /** 早鳥折扣率（0 < rate <= 1） */
    @NotNull
    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0（不折扣）")
    @Column(name = "early_bird_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal earlyBirdRate = new BigDecimal("0.80");

    /** 累積幾場活動（ATTENDED）才獲得一張優惠券 */
    @NotNull
    @Min(value = 1, message = "累積場數至少為1")
    @Column(name = "loyalty_threshold", nullable = false)
    private Integer loyaltyThreshold = 5;

    /** 忠誠優惠券折扣率 */
    @NotNull
    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0")
    @Column(name = "loyalty_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal loyaltyRate = new BigDecimal("0.50");

    /** 新人優惠券：是否啟用 */
    @Column(name = "welcome_coupon_enabled", nullable = true)
    private Boolean welcomeCouponEnabled = true;

    /** 新人優惠券折扣率（預設 0.80 = 八折） */
    @Column(name = "welcome_coupon_rate", nullable = true, precision = 4, scale = 2)
    private BigDecimal welcomeCouponRate = new BigDecimal("0.80");

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
