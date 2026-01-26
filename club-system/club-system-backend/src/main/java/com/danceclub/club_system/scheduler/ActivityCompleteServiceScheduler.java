package com.danceclub.club_system.scheduler;

import com.danceclub.club_system.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityCompleteServiceScheduler {

    @Autowired
    private ActivityService activityService;

    /**
     * 每小時檢查一次時間到期的活動，當時間大於結束時間，自動完成(如果沒有被取消的話)
     */

    @Scheduled(cron = "0 0 * * * ?")  //每小時整點實行
    public void autoCompleteExpiredActivities(){
        log.info("【Scheduler】開始檢查過期活動");
        activityService.completeExpiredActivities();
        log.info("【Scheduler】檢查完成");
    }
}
