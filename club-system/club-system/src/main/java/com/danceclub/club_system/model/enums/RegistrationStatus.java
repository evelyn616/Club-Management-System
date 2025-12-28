package com.danceclub.club_system.model.enums;

public enum RegistrationStatus {
/*報名狀態列舉
 */
    REGISTERED("已報名"),
    CANCELLED("已取消"),
    ATTENDED("已出席"),
    ABSENT("未出席");

    private final String displayName;

    RegistrationStatus(String displayName){this.displayName = displayName;}

    public String getDisplayName(){return displayName;}
}
