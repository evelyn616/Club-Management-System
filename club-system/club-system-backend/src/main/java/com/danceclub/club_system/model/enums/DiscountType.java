package com.danceclub.club_system.model.enums;

/**
 * 折扣類型
 * NONE      - 無折扣（全額）
 * OFFICER   - 幹部免費
 * EARLY_BIRD - 早鳥優惠（報名開始後N天內）
 * COUPON    - 優惠券（累積N場活動後獲得）
 */
public enum DiscountType {
    NONE,
    OFFICER,
    EARLY_BIRD,
    COUPON,
    PROMO_CODE
}
