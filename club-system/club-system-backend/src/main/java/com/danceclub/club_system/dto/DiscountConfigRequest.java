package com.danceclub.club_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理員更新全域折扣設定的請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountConfigRequest {

    @NotNull(message = "早鳥天數不得為空")
    @Min(value = 1, message = "早鳥天數至少為1天")
    private Integer earlyBirdDefaultDays;

    @NotNull(message = "早鳥折扣率不得為空")
    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0")
    private BigDecimal earlyBirdRate;

    @NotNull(message = "忠誠場數不得為空")
    @Min(value = 1, message = "累積場數至少為1")
    private Integer loyaltyThreshold;

    @NotNull(message = "忠誠折扣率不得為空")
    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0")
    private BigDecimal loyaltyRate;

    private Boolean welcomeCouponEnabled = true;

    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0")
    private BigDecimal welcomeCouponRate = new BigDecimal("0.80");
}
