package com.danceclub.club_system.model.enums;

/**
 * 費用類型列舉
 * ACTIVITY_FEE - 活動費用
 * MEMBERSHIP_FEE - 會員費
 * MATERIAL_FEE - 材料費
 * ANNUAL_FEE - 年費
 * OTHER - 其他
 */
public enum PaymentType {
    ACTIVITY_FEE("活動費用"),
    MEMBERSHIP_FEE("會員費"),
    MATERIAL_FEE("材料費"),
    ANNUAL_FEE("年費"),
    OTHER("其他");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
