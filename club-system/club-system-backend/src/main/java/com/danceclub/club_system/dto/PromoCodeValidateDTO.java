package com.danceclub.club_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeValidateDTO {
    private boolean valid;
    private String code;
    private String discountType;     // "FIXED_AMOUNT" | "PERCENTAGE"
    private BigDecimal discountValue;
    private String description;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private BigDecimal savedAmount;
    private String errorMessage;
}
