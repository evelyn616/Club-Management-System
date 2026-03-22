package com.danceclub.club_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequest {

    @NotBlank(message = "優惠碼不得為空")
    private String code;

    /** "FIXED_AMOUNT" | "PERCENTAGE" */
    @NotBlank(message = "折扣類型不得為空")
    private String discountType;

    @NotNull(message = "折扣值不得為空")
    @DecimalMin(value = "0.01", message = "折扣值至少 0.01")
    private BigDecimal discountValue;

    private String description;
    private Integer maxUsage;
    private LocalDateTime expiresAt;
}
