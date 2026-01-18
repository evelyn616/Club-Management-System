package com.danceclub.club_system.model.enums;

/**
 * 活動類型列舉
 */
public enum ActivityType {

    REGULAR("例行社課"),
    SPECIAL("特別活動"),
    TRAINING("團練"),
    PERFORMANCE("演出"),
    COMPETITION("競賽活動");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

