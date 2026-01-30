package com.danceclub.club_system.model.enums;

/**
 * 繳費狀態列舉
 * NOT_REQUIRED - 無須繳費（用於免費活動）
 * PENDING - 待繳費
 * PAID - 已繳費
 * CANCELLED - 已取消
 * REFUNDED - 已退款
 * PARTIAL_REFUNDED - 部分退款
 */
public enum PaymentStatus {
    NOT_REQUIRED("無須繳費"),
    PENDING("待繳費"),
    PAID("已繳費"),
    CANCELLED("已取消"),
    REFUNDED("已退款"),
    PARTIAL_REFUNDED("部分退款");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
