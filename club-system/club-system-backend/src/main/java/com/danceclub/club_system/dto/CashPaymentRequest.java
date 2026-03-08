package com.danceclub.club_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 現金付款請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashPaymentRequest {
    private String note; // 備註（可選）
}
