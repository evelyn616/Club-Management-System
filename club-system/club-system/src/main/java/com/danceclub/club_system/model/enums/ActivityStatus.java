package com.danceclub.club_system.model.enums;

/**
 * 活動狀態列舉
 */
public enum ActivityStatus {
    DRAFT("草稿"),
    PUBLISHED("已發布"),
    CANCELLED("已取消"),
    COMPLETED("已完成");

    private final String displayName;

    ActivityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
