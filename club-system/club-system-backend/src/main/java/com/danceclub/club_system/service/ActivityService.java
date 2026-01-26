package com.danceclub.club_system.service;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.ActivityType;
import com.danceclub.club_system.repository.ActivityRepository;
import com.danceclub.club_system.repository.RegistrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service

public class ActivityService {


    private final ActivityRepository activityRepository;
    private final RegistrationRepository registrationRepository;

    // TODO: 寫建構子，注入 activityRepository
    public ActivityService(ActivityRepository activityRepository, RegistrationRepository registrationRepository){
        this.activityRepository = activityRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<Activity>getAllActivities(){
        return activityRepository.findAll();
    }

    // TODO: 寫方法 - 取得所有已發布的活動
    public List<Activity>getAllPublishedActivities(){
        return activityRepository.findByStatusOrderByStartTimeAsc(ActivityStatus.PUBLISHED);
    }

    // TODO: 寫方法 - 根據類別取得已發布的活動
    public List<Activity>getPublishedActivitiesByType(ActivityType activityType){
        return activityRepository.findByActivityTypeAndStatusOrderByStartTimeAsc(activityType, ActivityStatus.PUBLISHED);
    }

    // TODO: 根據關鍵字搜尋已發布的活動
    public List<Activity> searchActivities(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            throw new IllegalArgumentException("搜尋關鍵字不可為空");
        }
        keyword = keyword.trim();
        if (keyword.length()>50){
            throw new IllegalArgumentException("搜尋關鍵字最多只能50個字喔!");

        }
        return activityRepository.searchByKeyword(keyword, ActivityStatus.PUBLISHED);
            }

    /**
     * 取得可報名的活動
     * (已發布 + 報名未截止 + 活動未開始)
     */
    public List<Activity> getRegistrableActivities() {
        // TODO: 呼叫 Repository
        LocalDateTime now = LocalDateTime.now();
        return activityRepository.findRegistrableActivities(ActivityStatus.PUBLISHED, now);
    }

    /**
     * 取得未來 7 天內的活動
     */
    public List<Activity> getUpcomingActivities() {
        // TODO: 呼叫 Repository
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);  // 要怎麼加 7 天？
        return activityRepository.findUpcomingActivities(ActivityStatus.PUBLISHED,now,sevenDaysLater);
    }


    /**
     * 根據 ID 查詢活動
     * @param id 活動 ID
     * @return 活動
     * @throws RuntimeException 如果找不到活動
     */
    public Activity getActivityById(Long id) {
        // TODO: 使用 findById 並在找不到時拋出例外
        return activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到活動 ID: " + id));
    }


    /**
     * 建立新活動
     * @param activity 活動資料
     * @return 已儲存的活動（含自動產生的 ID）
     */
    public Activity createActivity(Activity activity) {
        // TODO 1: 檢查 activity 是否為 null
        if (activity == null) {
            throw new IllegalArgumentException("活動資料不可為空");
        }

        // TODO 2: 設定預設狀態為 DRAFT
        activity.setStatus(ActivityStatus.DRAFT);


        // TODO 4: 儲存到資料庫
        return activityRepository.save(activity);
    }

    /**
     * 更新活動
     * @param id 活動 ID
     * @param updatedActivity 更新的活動資料
     * @return 更新後的活動
     */
    public Activity updateActivity(Long id, Activity updatedActivity) {
        // TODO 1: 先查詢活動是否存在（使用剛才寫的 getActivityById）
        Activity existingActivity = getActivityById(id);


        // TODO 3: 更新可修改的欄位
        existingActivity.setTitle(updatedActivity.getTitle());
        existingActivity.setDescription(updatedActivity.getDescription());
        existingActivity.setLocation(updatedActivity.getLocation());
        existingActivity.setStartTime(updatedActivity.getStartTime());
        existingActivity.setEndTime(updatedActivity.getEndTime());
        existingActivity.setMaxParticipants(updatedActivity.getMaxParticipants());
        existingActivity.setRegistrationDeadline(updatedActivity.getRegistrationDeadline());
        existingActivity.setFeeAmount(updatedActivity.getFeeAmount());
        existingActivity.setActivityType(updatedActivity.getActivityType());
        existingActivity.setTargetAudience(updatedActivity.getTargetAudience());
        existingActivity.setCoverImageUrl(updatedActivity.getCoverImageUrl());



        // TODO 4: 儲存
        return activityRepository.save(existingActivity);
    }

    /**
     * 刪除活動（僅限草稿狀態）
     * @param id 活動 ID
     */
    public void deleteActivity(Long id) {
        // TODO 1: 檢查活動是否存在
        Activity activity = getActivityById(id);

        // TODO 2: 檢查狀態是否為 DRAFT
        if (activity.getStatus() != ActivityStatus.DRAFT) {
            throw new IllegalStateException("只有草稿狀態的活動可以刪除，已發布的活動請使用取消功能");
        }

        // TODO 3: 刪除
        activityRepository.deleteById(id);
    }


    /**
     * 立即發布活動
     * @param id 活動 ID
     * @return 已發布的活動
     */
    @Transactional
    public Activity publishActivity(Long id) {
        // TODO 1: 查詢活動
        Activity activity = getActivityById(id);

        // TODO 2: 檢查當前狀態
        if (activity.getStatus() != ActivityStatus.DRAFT) {
            throw new IllegalStateException("只有草稿狀態的活動可以發布");
        }

        // TODO 3: 檢查必要欄位是否完整
        // 例如：標題、時間是否已填寫？
        if (activity.getTitle() == null || activity.getTitle().trim().isEmpty() ||
                activity.getStartTime() == null || activity.getEndTime() == null) {
            throw new IllegalStateException("活動資訊不完整，無法發布");
        }

        // TODO 4: 更新狀態並儲存
        activity.setStatus(ActivityStatus.PUBLISHED);
        activity.setPublishedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    /**
     * 預約發布活動
     * @param id 活動 ID
     * @return 已發布的活動
     */
    @Transactional
    public Activity schedulePublishActivity(Long id, LocalDateTime pulishedAt) {
        // TODO 1: 查詢活動
        Activity activity = getActivityById(id);

        // TODO 2: 檢查當前狀態
        if (activity.getStatus() != ActivityStatus.DRAFT) {
            throw new IllegalStateException("只有草稿狀態的活動可以發布");
        }

        //檢查時間
        if (pulishedAt.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("發布時間必須晚於現在");
        }

        // TODO 3: 設定預約發布
        // 例如：標題、時間是否已填寫？
        activity.setStatus(ActivityStatus.SCHEDULE);
        activity.setPublishedAt(pulishedAt);
        activity.setUpdatedAt(LocalDateTime.now());


        return activityRepository.save(activity);
    }

    /**
     * 取消預約發布
     */
    @Transactional
    public Activity cancelSchedulePublish(Long id){
        Activity activity = getActivityById(id);

        //查看狀態
        if (activity.getStatus() != ActivityStatus.SCHEDULE){
            throw new IllegalStateException("活動不是預約發布狀態");

        }

        //改回草稿
        activity.setStatus(ActivityStatus.DRAFT);
        activity.setUpdatedAt(LocalDateTime.now());
        activity.setPublishedAt(null);

        return activityRepository.save(activity);
    }

    /**
     * 取消活動
     * @param id 活動 ID
     * @return 已取消的活動
     */
    public Activity cancelActivity(Long id) {
        // TODO: 實作取消邏輯
        // 1. 查詢活動
        Activity activity = getActivityById(id);
        // 2. 檢查狀態（只有 PUBLISHED 可以取消）
        if (activity.getStatus() != ActivityStatus.PUBLISHED){
            throw new IllegalArgumentException("只有發布後的活動才能取消");
        }
        // 3. 更新狀態為 CANCELLED
        activity.setStatus(ActivityStatus.CANCELLED);
        return activityRepository.save(activity);
    }

    /**
     * 完成活動（標記為已完成）
     * @param id 活動 ID
     * @return 已完成的活動
     */
    public Activity completeActivity(Long id) {
        // TODO: 參考 cancelActivity 的結構
        // 1. 查詢活動
        Activity activity = getActivityById(id);
        // 2. 檢查狀態（只有 PUBLISHED 可以完成）
        if (activity.getStatus() != ActivityStatus.PUBLISHED){
            throw new IllegalArgumentException("只有發布後活動能完成");
        }
        // 3. 檢查活動是否已經結束（endTime < now）
        LocalDateTime now = LocalDateTime.now();
        if (activity.getEndTime().isAfter(now)){
            throw new IllegalArgumentException("活動尚未結束，不能標記為完成");
        }
        // 4. 更新狀態為 COMPLETED
        activity.setStatus(ActivityStatus.COMPLETED);
        return activityRepository.save(activity);
    }

    /**
     * 查詢某個使用者建立的所有活動
     * @param createdBy 建立者
     * @return 活動列表
     */
    public List<Activity> getActivitiesByCreator(String createdBy) {
        // TODO 1: 檢查 createdBy 是否為空
        if (createdBy == null) {
            throw new IllegalArgumentException("建立者不可為空");
        }

        // TODO 2: 呼叫 Repository
        return activityRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    /**
     * 檢查活動是否可以報名
     * @param id
     * @return 可否報名的布林值,1=可報名，0=不可報名
     */

    public boolean canRegisterActivity(Long id){
        //查詢活動
        Activity activity = getActivityById(id);
        //檢查活動本身是否可報名
        if (!activity.canRegister()){
            return false;
        }
        //檢查人數限制
        if (activity.hasParticipantLimit()){
            Long currentRegister = registrationRepository.countByActivityId(id);
            if (currentRegister >= activity.getMaxParticipants()){
                return false;
            }
        }

        return true;
    }

    /**
     * 驗證是否可以報名，給出exception
     * @param id
     * @return 若有錯誤，丟出錯誤訊息
     */
    public void validateCanRegister(Long id){
        //取得活動id
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到活動 ID: " + id));
        //檢查是否發布
        if (!activity.isPublished()){
            throw new IllegalStateException("活動尚未發布");
        }
        //檢查報名截止
        if (activity.isRegistrationClosed()){
            throw new IllegalStateException("活動已報名截止");
        }
        //檢查活動是否已開始
        if (activity.isOngoing()){
            throw new IllegalStateException("活動已開始，無法報名");
        }
        //檢查活動是否已結束
        if (activity.hasEnded()){
            throw new IllegalStateException("活動已結束");
        }
        //檢查人數限制
        if (activity.hasParticipantLimit()){
            Long currentValidRegister = registrationRepository.countValidRegistrations(id);
            if (currentValidRegister >= activity.getMaxParticipants()){
                throw new IllegalStateException("報名人數已滿");
            }
        }
    }

    /**
     * 自動完成已過期活動
     */
    @Transactional
    public void completeExpiredActivities(){
        LocalDateTime now = LocalDateTime.now();

        int updatedCount = activityRepository.updateStatus(
                ActivityStatus.PUBLISHED,
                ActivityStatus.COMPLETED,
                now
        );

    }


    /**
     * 查詢草稿活動
     */
    public List<Activity> getDraftActivities(){
        return activityRepository.findByStatusOrderByCreatedAtDesc(ActivityStatus.DRAFT);
    }

}