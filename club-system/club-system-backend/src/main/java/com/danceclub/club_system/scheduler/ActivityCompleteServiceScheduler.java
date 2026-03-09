package com.danceclub.club_system.scheduler;

import com.danceclub.club_system.service.ActivityService;
import com.danceclub.club_system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityCompleteServiceScheduler {

    private final ActivityService activityService;
    private final RegistrationService registrationService;

    /**
     * 每小時整點執行：
     * 1. 先將過期活動的未簽到報名標記為 ABSENT
     * 2. 再將過期活動標記為 COMPLETED
     *
     * 順序不可顛倒：markAbsent 需要活動還在 PUBLISHED 狀態才查得到
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoCompleteExpiredActivities() {
        log.info("【Scheduler】開始處理過期活動");

        // Step 1：標記未出席
        int absentCount = registrationService.markAbsentForExpiredActivities();
        log.info("【Scheduler】已標記 {} 筆報名為未出席", absentCount);

        // Step 2：完成活動
        activityService.completeExpiredActivities();
        log.info("【Scheduler】過期活動已完成");
    }
}
