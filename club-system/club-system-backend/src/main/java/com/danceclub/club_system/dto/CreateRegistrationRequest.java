package com.danceclub.club_system.dto;


import com.danceclub.club_system.model.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立報名的請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegistrationRequest {

    @NotNull(message = "活動ID不得為空")
    private Long activityId;

    @NotBlank(message = "會員ID不得為零")
    private String userId;

    /**
     * 會員希望使用的折扣類型（可不傳，預設 NONE）
     * OFFICER   → 幹部免費（後端會自動判斷，傳此值可明確宣告）
     * EARLY_BIRD → 早鳥（後端檢查截止時間）
     * COUPON     → 優惠券（需同時傳 loyaltyCouponId）
     * NONE       → 不使用折扣
     */
    private DiscountType requestedDiscount = DiscountType.NONE;

    /**
     * 當 requestedDiscount = COUPON 時，指定要使用哪一張券
     */
    private Long loyaltyCouponId;

    /** 優惠碼（選填，若填寫則優先使用優惠碼折扣） */
    private String promoCode;
}
