package com.danceclub.club_system.scheduler;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.repository.ActivityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class ActivityPublishScheduler {

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * 每分鐘檢查一次是否有需要發布的任務
     */

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void publishScheduledActivities(){

        //find all status=SCHEDULED 然後發布時間到的活動
        List<Activity> activitiesToPublish = activityRepository.findByStatusAndPublishedAtBefore(ActivityStatus.SCHEDULE, LocalDateTime.now());

        if (activitiesToPublish.isEmpty()){
            return;
        }

        //批次發布
        for (Activity activity : activitiesToPublish){
            activity.setStatus(ActivityStatus.PUBLISHED);
            activity.setUpdatedAt(LocalDateTime.now());
            activityRepository.save(activity);

            System.out.println("自動發布活動"+ activity.getTitle()+"(ID:"+activity.getId()+")");
        }

        System.out.println("成功發布" +activitiesToPublish.size() +"個預約活動");
    }
}
