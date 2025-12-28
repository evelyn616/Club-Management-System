package com.danceclub.club_system.model.enums;

/**
 * 參加對象列舉
 */
public enum TargetAudience {
    ALL("所有人"),
    MEMBER_ONLY("僅限社員"),
    MANAGER_ONLY("僅限幹部");

    private final String displayName;

    TargetAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}