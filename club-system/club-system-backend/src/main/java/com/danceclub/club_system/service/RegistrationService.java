package com.danceclub.club_system.service;


import com.danceclub.club_system.dto.*;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.service.PromoCodeService;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.DiscountType;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import com.danceclub.club_system.repository.ActivityRepository;
import com.danceclub.club_system.repository.PaymentRepository;
import com.danceclub.club_system.repository.RegistrationRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;
    private final PaymentRepository paymentRepository;
    public final UserService userService;
    private final EmailService emailService;
    private final DiscountService discountService;
    private final PromoCodeService promoCodeService;

    public RegistrationService(RegistrationRepository registrationRepository, @Lazy ActivityService activityService, PaymentRepository paymentRepository, UserService userService, EmailService emailService, ActivityRepository activityRepository, DiscountService discountService, PromoCodeService promoCodeService){
        this.registrationRepository = registrationRepository;
        this.activityService = activityService;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.activityRepository = activityRepository;
        this.discountService = discountService;
        this.promoCodeService = promoCodeService;
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
     * 建立新報名（含折扣計算）
     *
     * @param activityId        活動 id
     * @param userId            會員 id
     * @param requestedDiscount 會員想使用的折扣類型（NONE / EARLY_BIRD / COUPON）
     * @param loyaltyCouponId   當 requestedDiscount = COUPON 時，指定券 ID
     * @return 報名紀錄
     */
    public Registration createRegistration(Long activityId, String userId,
                                           DiscountType requestedDiscount, Long loyaltyCouponId,
                                           String promoCode) {
        Activity existingActivity = activityService.getActivityById(activityId);

        //檢查是否已到人數上限
        if(existingActivity.getMaxParticipants() != null){
            Long currentCount = registrationRepository.countValidRegistrations(activityId);
            if (currentCount >= existingActivity.getMaxParticipants()){
                throw new IllegalStateException("活動已額滿!");
            }
        }

        activityService.validateCanRegister(activityId);

        Optional<Registration> existing = registrationRepository
                .findByActivityIdAndUserId(activityId, userId);

        if (existing.isPresent()) {
            throw new IllegalStateException("您已報名過!");
        }

        // ==== 折扣計算 ====
        UserResponse user = userService.getUserById(userId);
        String userRole = user.getRole();

        DiscountService.DiscountResult discountResult;
        if (promoCode != null && !promoCode.isBlank()) {
            discountResult = promoCodeService.apply(promoCode, existingActivity.getFeeAmount());
        } else {
            DiscountType effectiveDiscount = requestedDiscount != null ? requestedDiscount : DiscountType.NONE;
            discountResult = discountService.calculate(existingActivity, userRole, effectiveDiscount, loyaltyCouponId);
        }

        // ==== 建立報名紀錄 ====
        Registration registration = new Registration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setDiscountType(discountResult.discountType);
        registration.setOriginalAmount(discountResult.originalAmount);
        if (promoCode != null && !promoCode.isBlank() && discountResult.discountType == DiscountType.PROMO_CODE) {
            registration.setPromoCodeUsed(promoCode.toUpperCase().trim());
        }

        boolean isFinallyFree = discountResult.finalAmount.compareTo(BigDecimal.ZERO) == 0;

        if (isFinallyFree) {
            registration.setPaymentStatus(PaymentStatus.NOT_REQUIRED);
            registration.setPaymentAmount(BigDecimal.ZERO);
        } else {
            registration.setPaymentStatus(PaymentStatus.PENDING);
            registration.setPaymentAmount(discountResult.finalAmount);
        }

        Registration saved = registrationRepository.save(registration);

        // 若使用忠誠優惠券，標記為已使用
        if (discountResult.discountType == DiscountType.COUPON && discountResult.usedCoupon != null) {
            discountService.markCouponUsed(discountResult.usedCoupon.getId(), saved.getId());
        }

        // 如果需要繳費，建立 payment 紀錄
        if (!isFinallyFree) {
            createPaymentForRegistration(saved, existingActivity, discountResult);
        }

        return saved;
    }

    /**
     * 向下相容的舊版方法（不傳折扣參數）
     */
    public Registration createRegistration(Long activityId, String userId) {
        return createRegistration(activityId, userId, DiscountType.NONE, null, null);
    }

    /**
     * 向下相容的四參數版本
     */
    public Registration createRegistration(Long activityId, String userId,
                                            DiscountType requestedDiscount, Long loyaltyCouponId) {
        return createRegistration(activityId, userId, requestedDiscount, loyaltyCouponId, null);
    }

    /**
     * 報名建立繳費紀錄（含折扣資訊）
     */
    private void createPaymentForRegistration(Registration registration, Activity activity,
                                              DiscountService.DiscountResult discountResult){
        Payment payment = new Payment();
        payment.setRegistration(registration);
        payment.setPaymentType(PaymentType.ACTIVITY_FEE);

        BigDecimal originalAmount = discountResult.originalAmount;
        BigDecimal finalAmount    = discountResult.finalAmount;

        payment.setOriginalAmount(originalAmount);
        payment.setAmount(finalAmount);

        // 折扣金額
        BigDecimal discountAmt = originalAmount.subtract(finalAmount);
        if (discountAmt.compareTo(BigDecimal.ZERO) > 0) {
            payment.setDiscountAmount(discountAmt);
            payment.setDiscountReason(discountResult.discountType.name());
        }

        // 繳費期限設定為活動開始前三天
        if (activity.getStartTime() != null){
            payment.setPaymentDeadline(activity.getStartTime().minusDays(3));
        }
        payment.setNote("系統自動建立");

        paymentRepository.save(payment);
    }

    /**
     * 向下相容：無折扣版本（由舊 createRegistration 呼叫）
     * @deprecated 請改用帶 DiscountResult 的版本
     */
    @Deprecated
    private void createPaymentForRegistration(Registration registration, Activity activity){
        Payment payment = new Payment();
        payment.setRegistration(registration);
        payment.setPaymentType(PaymentType.ACTIVITY_FEE);

        BigDecimal originalAmount = activity.getFeeAmount();
        payment.setOriginalAmount(originalAmount);
        payment.setAmount(originalAmount);

        if (activity.getStartTime() != null){
            payment.setPaymentDeadline(activity.getStartTime().minusDays(3));
        }
        payment.setNote("系統自動建立");

        paymentRepository.save(payment);
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
     * 同步更新 Registration.paymentStatus 及 Payment.status
     * @param id 報名 id
     * @return 取消後的報名
     */
    public Registration cancelRegistration(Long id) {
        // 1. 查詢報名紀錄
        Registration registration = getRegistrationById(id);

        // 2. 查詢活動，確認活動尚未開始
        Activity activity = activityService.getActivityById(registration.getActivityId());
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(activity.getStartTime())) {
            throw new IllegalStateException("活動開始無法取消報名喔!");
        }

        // 3. 確認可以取消（未簽到）
        if (!registration.canCancel()) {
            throw new IllegalStateException("簽到後無法取消報名!");
        }

        // 4. 更新報名狀態
        registration.setStatus(RegistrationStatus.CANCELLED);

        // 5. 同步更新繳費狀態
        PaymentStatus currentPaymentStatus = registration.getPaymentStatus();

        if (PaymentStatus.PENDING.equals(currentPaymentStatus)) {
            // 尚未繳費：直接取消繳費
            registration.setPaymentStatus(PaymentStatus.CANCELLED);

            // 同步更新 Payment entity（若存在）
            paymentRepository.findByRegistration(registration).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(payment);
            });

        } else if (PaymentStatus.PAID.equals(currentPaymentStatus)) {
            // 已繳費：標記為待退款（REFUNDED），由管理員後續處理退款
            registration.setPaymentStatus(PaymentStatus.REFUNDED);

            // 同步更新 Payment entity（若存在）
            paymentRepository.findByRegistration(registration).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            });
        }
        // NOT_REQUIRED（免費活動）：繳費狀態不動

        // 6. 儲存報名紀錄
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

    /**
     * 取得活動帶繳費紀錄
     * 用於批量提醒的察看
     */
    public List<Registration> getActivityPendingPayments(Long activityId){
        if (activityId == null){
            throw new IllegalArgumentException("活動ID不得為空");
        }
        return registrationRepository.findByActivityIdAndPaymentStatus(activityId, PaymentStatus.PENDING);
    }


    /**
     * 提醒繳費
     */
    public void sendPaymentReminder(Long id){

        Registration registration = getRegistrationById(id);

        Activity activity = activityService.getActivityById(registration.getActivityId());

        Payment payment = paymentRepository.findByRegistration(registration)
                .orElseThrow(() -> new RuntimeException("找不到繳費紀錄"));

        if(payment.getStatus() == PaymentStatus.PAID){
            throw new IllegalStateException("已完成繳費，無須提醒");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED||
                payment.getStatus() == PaymentStatus.REFUNDED
        ){
            throw new IllegalStateException("此繳費已取消或已退款");
        }

        UserResponse user = userService.getUserById(registration.getUserId());

        //發送郵件
        emailService.sendPaymentReminder(
                user.getEmail(),
                user.getName(),
                activity.getTitle(),
                payment.getAmount(),
                payment.getPaymentDeadline()
        );

    }
    /**
     * 批量提醒繳費發送
     */
    public Map<String, Object> sendBatchPaymentReminders(Long activityId){
        List<Registration> pendingRegistrations = getActivityPendingPayments(activityId);

        int successCount = 0;
        int failCount = 0;
        List<String> failedUsers = new ArrayList<>();

        for (Registration registration : pendingRegistrations){
            try{
                sendPaymentReminder((registration.getId()));
                successCount++;
            }
            catch (Exception e){
                failCount++;
                failedUsers.add(registration.getUserId());
                System.out.println("發送失敗:registrationId = "+registration.getId()+"，原因"+e.getMessage());
            }

        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedUsers", failedUsers);
        result.put("total", pendingRegistrations.size());

        return result;
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
        Registration saved = registrationRepository.save(registration);

        // 簽到後檢查忠誠優惠券資格（冪等，不影響主流程）
        try {
            discountService.checkAndAwardLoyaltyCoupons(registration.getUserId());
        } catch (Exception e) {
            System.err.println("忠誠優惠券發放失敗（不影響簽到）: " + e.getMessage());
        }

        return saved;
    }

    @Transactional
    public int markAbsentForExpiredActivities() {
        LocalDateTime now = LocalDateTime.now();

        List<Long> expiredActivityIds =
                activityRepository.findExpiredActivityIds(ActivityStatus.COMPLETED, now);

        if (expiredActivityIds.isEmpty()) return 0;

        return registrationRepository.markAbsentByActivityIds(
                expiredActivityIds,
                RegistrationStatus.REGISTERED,
                RegistrationStatus.ABSENT
        );
    }

    /**
     * 透過 userId + activityId 執行簽到（QR Code 掃碼流程）
     * 前端不需要先查 registrationId，由後端自行查找後執行
     *
     * @param userId     會員 ID
     * @param activityId 活動 ID
     * @return 更新後的報名紀錄
     */
    public Registration checkInByUser(String userId, Long activityId) {
        // 1. 驗證參數
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId 不可為空");
        }
        if (activityId == null) {
            throw new IllegalArgumentException("activityId 不可為空");
        }

        // 2. 查詢此會員對此活動的報名紀錄
        Registration registration = registrationRepository
                .findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new IllegalStateException("查無報名紀錄，請確認是否已完成報名"));

        // 3. 委派給現有的 checkIn(id) 執行完整的簽到邏輯
        //    （防重複簽到、繳費檢查、遲到判斷、狀態更新 都在 checkIn 裡處理）
        return checkIn(registration.getId());
    }

    //====查詢某活動的所有報名紀錄====//
    //1.檢查活動的id是否為空
    //2.使用 findByActivityIdByRegistrationTimeAsc
    /**
     * 查詢某活動的報名紀錄
     * @param activityId
     * @return 活動報名紀錄列表
     */

    public List<RegistrationWithUserDTO> getActivityRegistrations(Long activityId){
        if (activityId == null){
            throw new IllegalArgumentException("活動id不可為空");
        }
        //查詢所有報名
        List<Registration> allRegistrations = registrationRepository
                .findByActivityIdOrderByRegistrationTimeAsc(activityId);
        //有效報名名單
        return allRegistrations.stream()
                .filter(r -> !RegistrationStatus.CANCELLED.equals(r.getStatus()))
                .map(reg -> {
                    String userName;
                    try {
                        UserResponse user = userService.getUserById(reg.getUserId());
                        userName = user.getName();
                    } catch (Exception e) {
                        userName = null;
                    }

                    return RegistrationWithUserDTO.from(reg, userName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 查詢會員報名紀錄（含活動、繳費詳情）- 用於「我的報名」頁面
     * @param userId 會員 ID
     * @return 含活動標題、地點、時間、繳費狀態的報名列表
     */
    public List<RegistrationDetailDTO> getUserRegistrationsDetail(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("會員ID不可為空");
        }

        //  查報名紀錄
        List<Registration> registrations =
                registrationRepository.findByUserIdOrderByRegistrationTimeDesc(userId);

        //  查會員資訊（一次，所有報名共用）
        UserResponse user = userService.getUserById(userId);

        // 整合
        return registrations.stream()
                .map(reg -> {
                    // 查活動
                    Activity activity = null;
                    try {
                        activity = activityService.getActivityById(reg.getActivityId());
                    } catch (Exception ignored) { }

                    // 查繳費紀錄（可能為 null）
                    Payment payment = null;
                    try {
                        payment = paymentRepository.findByRegistration(reg).orElse(null);
                    } catch (Exception ignored) { }

                    return RegistrationDetailDTO.from(reg, user, activity, payment);
                })
                .collect(Collectors.toList());
    }

    /**
     *查詢所有有效報名紀錄
     */
    public List<Registration> getAllRegistrations(){
         return registrationRepository.findByStatusNot(RegistrationStatus.CANCELLED);
        //有效報名名單

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



    /**
     * 查詢單筆報名的詳細資訊（含會員、活動、繳費）
     * 用途：管理員查看報名詳情頁面
     */
    public RegistrationDetailDTO getRegistrationDetail(Long id){
        Registration registration = getRegistrationById(id);
        Activity activity = activityService.getActivityById(registration.getActivityId());
        UserResponse user = userService.getUserById(registration.getUserId());
        Payment payment = paymentRepository.findByRegistration(registration).orElse(null);

        return RegistrationDetailDTO.from(registration, user, activity, payment);
    }

    /**
     * 查詢會員的所有報名（含活動摘要、能否取消）
     * 用途：會員端「我的報名」頁面
     */
    public List<UserRegistrationDTO> getUserRegistrationsWithDetails(String userId){
        if (userId == null || userId.trim().isEmpty()){
            throw new IllegalArgumentException("會員ID不可為空");
        }

        List<Registration> registrations = registrationRepository.findByUserIdOrderByRegistrationTimeDesc(userId);

        return registrations.stream().map(registration -> {
            Activity activity = activityService.getActivityById(registration.getActivityId());
            Payment payment = paymentRepository.findByRegistration(registration).orElse(null);
            return UserRegistrationDTO.from(registration, activity, payment);
        }).collect(Collectors.toList());
    }



}
