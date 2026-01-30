package com.danceclub.club_system.model.enums;

/**
 * 付款方式列舉
 * CASH - 現金
 * BANK_TRANSFER - 銀行轉帳
 * CREDIT_CARD - 信用卡
 * LINE_PAY - LINE Pay
 * OTHER - 其他
 */
public enum PaymentMethod {
    CASH("現金"),
    BANK_TRANSFER("銀行轉帳"),
    CREDIT_CARD("信用卡"),
    LINE_PAY("LINE Pay"),
    OTHER("其他");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
