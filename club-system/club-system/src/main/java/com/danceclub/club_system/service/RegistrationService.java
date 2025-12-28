package com.danceclub.club_system.service;


import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import com.danceclub.club_system.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ActivityService activityService;

    public RegistrationService(RegistrationRepository registrationRepository, ActivityService activityService){
        this.registrationRepository = registrationRepository;
        this.activityService = activityService;
    }

    /**
     * 根據ID查詢報名
     * @param id 報名id
     * @return 報名紀錄
     * @throws RuntimeException 如果找不到活動
     */

    public Registration getRegistrationById(Long id){
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到報名紀錄"+ id));
    }

    /**
     * 根據某會員的報名紀錄
     * @param userId 會員id
     * @return 報名紀錄
     * @throws RuntimeException 如果找不到活動
     */

    public List<Registration> getUserRegistrations(String userId){
        if (userId==null||userId.trim().isEmpty()){
            throw new IllegalArgumentException("會員ID不可為空");
        }
        return registrationRepository.findByUserIdOrderByRegistrationTimeDesc(userId);
    }

    //====建立報名====//

    //1.先從ActivityService檢查活動id是否存在
    //2.從RegistrationRepository看是否存在重複報名的狀況發生(check findByActivityIdAndUserId)
    //   -如果已經報名，要出現一個exception"您已經報名"
    //3.檢查完畢，都沒有問題，則建立Registration物件
    //4.設定活動不需要繳費，繳多少
    //   -如果活動是免費的：activity.requirePayment() == false,paymentStatus = NOT_REQUIRED,paymentAmount = BigDecimal.ZERO
    //   -如果活動要錢：activity.requirePayment() == true,paymentStatus = PENDING,paymentAmount = activity.getFeeAmount
    //5.儲存並回傳

    /**
     * 建立新報名
     * @param activityId 活動id
     * @param userId 會員id
     * @return 報名紀錄
     */

    public Registration createRegistration(Long activityId, String userId) {
        Activity existingActivity = activityService.getActivityById(activityId);


        activityService.validateCanRegister(activityId);

        Optional<Registration> existing = registrationRepository
                .findByActivityIdAndUserId(activityId, userId);

        if (existing.isPresent()) {
            throw new IllegalStateException("您已報名過!");
        }

        Registration registration = new Registration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);

        if (existingActivity.requiresPayment()) {

            registration.setPaymentStatus(PaymentStatus.PENDING);
            registration.setPaymentAmount(existingActivity.getFeeAmount());
        } else {
            registration.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
            registration.setPaymentAmount(BigDecimal.ZERO);
        }



        Registration saved = registrationRepository.save(registration);


        return saved;
    }

    //====取消報名====//
    //1.查詢報名紀錄(getRegistrationId)
    //2.檢查是否具備取消資格
    //  -在活動開始之前才能取消
    //  -簽到後不能取消
    //  -若不能取消，丟出exception
    //3.儲存狀態為CANCEL
    //4.存到資料庫

    /**
     * 取消報名
     * @param id
     * @return 取消的報名
     */

    public Registration cancelRegistration(Long id){
        //找尋報名紀錄
        Registration registration = getRegistrationById(id);
        //查詢活動
        Activity activity = activityService.getActivityById(registration.getActivityId());
        //檢查活動是否已開始
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(activity.getStartTime())){
            throw new IllegalStateException("活動開始無法取消報名喔!");
        }
        //檢查是否已簽到
        if (!registration.canCancel()){
            throw new IllegalStateException("簽到後無法取消報名!");
        }
        //儲存狀態為CANCEL
        registration.setStatus(RegistrationStatus.CANCELLED);
        return registrationRepository.save(registration);
    }

    //====繳費相關====//
    //A.更新繳費狀態
    //1.查詢報名紀錄
    //2.檢查繳費狀態是否為PENDING
    //  -如果不是，則不用更新，丟出exception
    //3.把PENDING轉為PAID
    //4.更新到資料庫

    /**
     * 更新繳費狀態
     * @param id 報名id
     * @return 更新後的繳費狀態
     */
    public Registration updatePaymentStatus(Long id){
        //查詢報名紀錄
        Registration registration = getRegistrationById(id);
        //檢查繳費狀態
        if (!PaymentStatus.PENDING.equals(registration.getPaymentStatus())){
            throw new IllegalStateException("此報名不需要繳費或是已繳費");
        }
        registration.setPaymentStatus(PaymentStatus.PAID);
        return registrationRepository.save(registration);
    }

    //B.查詢某會員的代繳費活動
    //1.檢查userId是否為空
    //2.用findByUserIdAndPaymentStatus做查詢

    /**
     * 查詢會員的待繳費報名
     * @param userId
     * @return 待繳費的報名紀錄列表
     */
    public List<Registration> getPendingPayments(String userId){
        if (userId == null || userId.trim().isEmpty()){
            throw new IllegalArgumentException("userId不可為空");
        }
        return registrationRepository.findByUserIdAndPaymentStatus(userId,PaymentStatus.PENDING);
    }

    //====簽到相關====//
    //執行簽到邏輯
    //1.查詢報名紀錄
    //2.查詢活動(activity.getActivityId)
    //3.檢查是否已經簽到過
    //  -如果有，丟出exception
    //4.檢查繳費狀態
    //  -如果需要繳費但沒有繳，就丟exception
    //5.設定簽到狀態
    //  -未遲到狀態下簽到：checkIn =true
    //  -checkInTime = LocalDateTime.now()
    //  -判斷遲到：簽到時間 > 活動開始時間+30分鐘
    //6.更新狀態為ATTEND
    //7.儲存到資料庫

    /**
     * 紀錄簽到
     * @param id 報名id
     * @return 更新後的報名狀態
     */
    public Registration checkIn(Long id){
        //查詢報名紀錄
        Registration registration = getRegistrationById(id);
        //查詢活動id
        Activity activity = activityService.getActivityById(registration.getActivityId());
        //檢查是否已簽到過
        if (registration.isCheckedIn()){
            throw new IllegalStateException("您已簽到過");
        }
        //檢查報名狀態
        if (!RegistrationStatus.REGISTERED.equals(registration.getStatus())){
            throw new IllegalStateException("此報名狀態無法簽到");
        }

        //檢查繳費狀態
        if (registration.requiresPayment()){
            throw new IllegalStateException("未繳完款不得簽到，請先完成繳費，謝謝合作!");
        }
        //設定簽到狀態
        LocalDateTime now = LocalDateTime.now();
        registration.setCheckedIn(true);
        registration.setCheckInTime(now);
        //判斷遲到
        LocalDateTime lateDateTime = activity.getStartTime().plusMinutes(30);
        if (now.isAfter(lateDateTime)){
            registration.setIsLate(true);
        }
        else{
            registration.setIsLate(false);
        }
        //更新狀態
        registration.setStatus(RegistrationStatus.ATTENDED);
        return registrationRepository.save(registration);
    }

    //====查詢某活動的所有報名紀錄====//
    //1.檢查活動的id是否為空
    //2.使用 findByActivityIdByRegistrationTimeAsc
    /**
     * 查詢某活動的報名紀錄
     * @param activityId
     * @return 活動報名紀錄列表
     */

    public List<Registration> getActivityRegistration(Long activityId){
        if (activityId == null){
            throw new IllegalArgumentException("活動id不可為空");
        }
        //查詢所有報名
        List<Registration> allRegistrations = registrationRepository
                .findByActivityIdOrderByRegistrationTimeAsc(activityId);
        //有效報名名單
        return allRegistrations.stream()
                .filter(r -> !RegistrationStatus.CANCELLED.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    //====統計某活動報名人數====//
    //1.檢查活動id是否為空
    //2.使用registrationRepository.countByActivityId

    /**
     * 統計所有報名人數（包含已取消）- 用於統計報表
     * @param activityId 活動 ID
     * @return 報名總人數(所有)
     */
    public Long countRegistrations(Long activityId){
        if (activityId == null){
            throw new IllegalArgumentException("活動id不可為空");
        }
        return registrationRepository.countByActivityId(activityId);
    }

    /**
     * 統計有效報名人數（排除已取消）- 用於人數限制檢查
     */
    public Long countValidRegistrations(Long activityId) {
        if (activityId == null) {
            throw new IllegalArgumentException("活動id不可為空");
        }
        return registrationRepository.countValidRegistrations(activityId);
    }



    //====統計某活動的簽到人數====//
    //1.檢查活動id是否為空
    //2.使用registrationRepository.countByActivityIdAndCheckedIn

    /**
     * 統計某活動的簽到人數
     * @param activityId 活動 ID
     * @return 簽到人數
     */
    public Long countCheckedIn(Long activityId){
        if (activityId == null){
            throw new IllegalArgumentException("活動id不可為空");
        }
        return registrationRepository.countByActivityIdAndCheckedInTrue(activityId);
    }
}
