package com.danceclub.club_system.model.enums;

/**
 * 活動類型列舉
 */
public enum ActivityType {
    ALL("全部"),
    WHATS_ON("What's On"),
    NEW_ONSALES("New Onsales"),
    REGULAR("例行社課"),
    OUTDOOR("外出活動"),
    TRAINING("幹部訓練"),
    ORIENTATION("迎新活動"),
    COMPETITION("競賽活動");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

