package com.danceclub.club_system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理員手動發放優惠券請求
 * discountRate 與 discountAmount 擇一填寫
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponRequest {

    @NotBlank(message = "userId 不得為空")
    private String userId;

    /** 折扣率（0.01~1.0），與 discountAmount 擇一 */
    @DecimalMin(value = "0.01", message = "折扣率至少 0.01")
    @DecimalMax(value = "1.0",  message = "折扣率最多 1.0")
    private BigDecimal discountRate;

    /** 固定折扣金額，與 discountRate 擇一 */
    @DecimalMin(value = "1", message = "折扣金額至少 1 元")
    private BigDecimal discountAmount;

    private String description;
}
