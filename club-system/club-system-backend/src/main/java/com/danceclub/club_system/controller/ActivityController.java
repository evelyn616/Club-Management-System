package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.ActivityResponse;
import com.danceclub.club_system.dto.SchedulePublishRequest;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.ActivityType;
import com.danceclub.club_system.repository.ActivityRepository;
import com.danceclub.club_system.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    private final ActivityService activityService;

    // TODO 1: 建構子
    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }
    // GET /api/activities/{id}
    // TODO 2: 根據 ID 取得單一活動
    @GetMapping("/{id}")
    public Activity getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    // GET /api/activities 或 /api/activities?type=REGULAR
    @GetMapping
    public List<Activity> getActivities(
            @RequestParam(required = false) ActivityType type
    ) {
        // TODO 3: 判斷 type 是否有值
        if (type == null) {
            // 回傳所有已發布活動
            return activityService.getAllActivities();
        } else {
            // 回傳特定類別的活動
            return activityService.getPublishedActivitiesByType(type);
        }
    }

    // TODO 4: 關鍵字搜尋
    // GET /api/activities/search?keyword=聖誕
    @GetMapping("/search")
    public List<Activity> searchActivities(@RequestParam String keyword) {
        return activityService.searchActivities(keyword);
    }

    // TODO 5: 取得可報名的活動
    // GET /api/activities/registrable
    @GetMapping("/registrable")
    public List<Activity> getRegistrableActivities() {
        return activityService.getRegistrableActivities();
    }

    // TODO 6: 取得未來 7 天內的活動
    // GET /api/activities/upcoming
    @GetMapping("/upcoming")
    public List<Activity> getUpcomingActivities() {
        return activityService.getUpcomingActivities();
    }

    // TODO 7: 查詢某個使用者建立的活動
    // GET /api/activities/creator/{createdBy}
    @GetMapping("/creator/{createdBy}")
    public List<Activity> getActivitiesByCreator(@PathVariable String createdBy) {
        return activityService.getActivitiesByCreator(createdBy);
    }
    // TODO 8: 建立新活動
    // POST /api/activities
    @PostMapping
    public Activity createActivity(@Valid @RequestBody Activity activity) {
        return activityService.createActivity(activity);
    }

    // TODO 9: 更新活動
    // PUT /api/activities/{id}
    @PutMapping("/{id}")
    public Activity updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody Activity activity
    ) {
        return activityService.updateActivity(id,activity);
    }

    // TODO 10: 刪除活動
    // DELETE /api/activities/{id}
    @DeleteMapping("/{id}")
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }

    // TODO 11: 發布活動
    // PUT /api/activities/{id}/publish
    @PutMapping("/{id}/publish")
    public ResponseEntity<ActivityResponse> publishActivity(
            @PathVariable Long id){
        Activity activity = activityService.publishActivity(id);
        return ResponseEntity.ok(ActivityResponse.from(activity));
    }


    //預約發布活動
    @PutMapping("/{id}/schedule-publish")
    public ResponseEntity<ActivityResponse> schedulePublishActivity(
            @PathVariable Long id,
            @Valid @RequestBody SchedulePublishRequest request) {

        Activity activity =activityService.schedulePublishActivity(id, request.getPublishAt());
        return ResponseEntity.ok(ActivityResponse.from(activity));
    }

    //取消預約
    @PutMapping("/{id}/cancel-schedule")
    public ResponseEntity<ActivityResponse> cancelSchedulePublish(
            @PathVariable Long id
    ){
        Activity activity=activityService.cancelSchedulePublish(id);
        return ResponseEntity.ok(ActivityResponse.from(activity));
    }

    // TODO 12: 取消活動
    // PUT /api/activities/{id}/cancel
    @PutMapping("/{id}/cancel")
    public Activity cancelActivity(@PathVariable Long id) {
        return activityService.cancelActivity(id);
    }

    // TODO 13: 完成活動
    // PUT /api/activities/{id}/complete
    @PutMapping("/{id}/complete")
    public Activity completeActivity(@PathVariable Long id) {
        return activityService.completeActivity(id);
    }

    //查詢草稿活動
    @GetMapping("/drafts")
    public List<Activity> getDraftActivities(){return activityService.getDraftActivities();}
}