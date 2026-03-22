package com.danceclub.club_system.service;

import com.danceclub.club_system.dto.CouponAdminDTO;
import com.danceclub.club_system.dto.DiscountConfigRequest;
import com.danceclub.club_system.dto.DiscountPreviewDTO;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.User;
import com.danceclub.club_system.repository.UserRepository;
import com.danceclub.club_system.model.DiscountConfig;
import com.danceclub.club_system.model.LoyaltyCoupon;
import com.danceclub.club_system.model.enums.DiscountType;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import com.danceclub.club_system.repository.DiscountConfigRepository;
import com.danceclub.club_system.repository.LoyaltyCouponRepository;
import com.danceclub.club_system.repository.RegistrationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final DiscountConfigRepository discountConfigRepository;
    private final LoyaltyCouponRepository loyaltyCouponRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public DiscountService(
            DiscountConfigRepository discountConfigRepository,
            LoyaltyCouponRepository loyaltyCouponRepository,
            RegistrationRepository registrationRepository,
            UserRepository userRepository
    ) {
        this.discountConfigRepository = discountConfigRepository;
        this.loyaltyCouponRepository = loyaltyCouponRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    // =====================================================================
    //  DiscountConfig CRUD
    // =====================================================================

    /**
     * 取得全域折扣設定；若尚未建立，自動以預設值初始化。
     */
    public DiscountConfig getConfig() {
        return discountConfigRepository.findById(1L).orElseGet(() -> {
            DiscountConfig config = new DiscountConfig();
            config.setId(1L);
            return discountConfigRepository.save(config);
        });
    }

    /**
     * 管理員更新全域折扣設定
     */
    @Transactional
    public DiscountConfig updateConfig(DiscountConfigRequest request, String updatedBy) {
        DiscountConfig config = getConfig();
        config.setEarlyBirdDefaultDays(request.getEarlyBirdDefaultDays());
        config.setEarlyBirdRate(request.getEarlyBirdRate());
        config.setLoyaltyThreshold(request.getLoyaltyThreshold());
        config.setLoyaltyRate(request.getLoyaltyRate());
        if (request.getWelcomeCouponEnabled() != null) {
            config.setWelcomeCouponEnabled(request.getWelcomeCouponEnabled());
        }
        if (request.getWelcomeCouponRate() != null) {
            config.setWelcomeCouponRate(request.getWelcomeCouponRate());
        }
        config.setUpdatedBy(updatedBy);
        return discountConfigRepository.save(config);
    }

    // =====================================================================
    //  Early Bird 判斷
    // =====================================================================

    /**
     * 計算某活動的早鳥截止時間。
     * 若活動有自訂 earlyBirdDeadline 則用之，否則以發布時間 + 全域預設天數計算。
     */
    public LocalDateTime calcEarlyBirdDeadline(Activity activity) {
        if (activity.getEarlyBirdDeadline() != null) {
            return activity.getEarlyBirdDeadline();
        }
        // 以發布時間為基準；若尚未發布則以 registrationDeadline 前推
        LocalDateTime base = activity.getPublishedAt() != null
                ? activity.getPublishedAt()
                : activity.getRegistrationDeadline().minusDays(7);
        int days = getConfig().getEarlyBirdDefaultDays();
        return base.plusDays(days);
    }

    /**
     * 判斷早鳥優惠是否仍有效（截止時間尚未過）
     */
    public boolean isEarlyBirdActive(Activity activity) {
        if (!activity.requiresPayment()) return false;
        LocalDateTime deadline = calcEarlyBirdDeadline(activity);
        return LocalDateTime.now().isBefore(deadline);
    }

    // =====================================================================
    //  折扣預覽（報名前呼叫）
    // =====================================================================

    /**
     * 回傳某會員報名某活動時可享有的折扣資訊
     *
     * @param activity 活動
     * @param userRole 會員角色（"officer" / "member" / "admin"）
     * @param userId   會員 ID
     */
    public DiscountPreviewDTO preview(Activity activity, String userRole, String userId) {
        DiscountConfig config = getConfig();
        BigDecimal original = activity.getFeeAmount();

        boolean officerFree = "officer".equalsIgnoreCase(userRole);

        boolean earlyBirdActive = !officerFree && isEarlyBirdActive(activity);
        LocalDateTime earlyBirdDeadline = calcEarlyBirdDeadline(activity);
        BigDecimal earlyBirdRate = config.getEarlyBirdRate();
        BigDecimal earlyBirdAmount = earlyBirdActive
                ? original.multiply(earlyBirdRate).setScale(2, RoundingMode.HALF_UP)
                : null;

        List<LoyaltyCoupon> coupons = loyaltyCouponRepository
                .findByUserIdAndIsUsedFalseOrderByEarnedAtAsc(userId);

        List<DiscountPreviewDTO.LoyaltyCouponDTO> couponDTOs = coupons.stream()
                .map(c -> {
                    BigDecimal cRate = c.getDiscountRate() != null ? c.getDiscountRate() : config.getLoyaltyRate();
                    return new DiscountPreviewDTO.LoyaltyCouponDTO(c.getId(), c.getEarnedAt(), cRate,
                            c.getDescription(), c.getCouponType(), c.getDiscountAmount());
                })
                .collect(Collectors.toList());

        BigDecimal loyaltyRate = config.getLoyaltyRate();
        BigDecimal loyaltyAmount = !coupons.isEmpty()
                ? original.multiply(couponDTOs.get(0).getDiscountRate()).setScale(2, RoundingMode.HALF_UP)
                : null;

        DiscountPreviewDTO dto = new DiscountPreviewDTO();
        dto.setOriginalAmount(original);
        dto.setOfficerFree(officerFree);
        dto.setEarlyBirdActive(earlyBirdActive);
        dto.setEarlyBirdDeadline(earlyBirdDeadline);
        dto.setEarlyBirdRate(earlyBirdRate);
        dto.setEarlyBirdAmount(earlyBirdAmount);
        dto.setAvailableCoupons(couponDTOs);
        dto.setLoyaltyRate(loyaltyRate);
        dto.setLoyaltyAmount(loyaltyAmount);
        return dto;
    }

    // =====================================================================
    //  折扣計算（在 RegistrationService.createRegistration 中呼叫）
    // =====================================================================

    /**
     * 根據角色 / 請求類型計算實際應收金額與折扣類型。
     *
     * @param activity         活動
     * @param userRole         會員角色
     * @param requestedDiscount 會員選擇的折扣類型（COUPON or EARLY_BIRD or NONE）
     * @param loyaltyCouponId  若選 COUPON，需提供券 ID
     * @return DiscountResult
     */
    public DiscountResult calculate(
            Activity activity,
            String userRole,
            DiscountType requestedDiscount,
            Long loyaltyCouponId
    ) {
        BigDecimal original = activity.getFeeAmount();
        DiscountConfig config = getConfig();

        // 1. 幹部免費（強制，無視請求）
        if ("officer".equalsIgnoreCase(userRole)) {
            return new DiscountResult(DiscountType.OFFICER, original, BigDecimal.ZERO, null);
        }

        // 2. 如果活動本來就免費，不套折扣
        if (!activity.requiresPayment()) {
            return new DiscountResult(DiscountType.NONE, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }

        // 3. 忠誠優惠券
        if (requestedDiscount == DiscountType.COUPON && loyaltyCouponId != null) {
            LoyaltyCoupon coupon = loyaltyCouponRepository.findById(loyaltyCouponId)
                    .orElseThrow(() -> new IllegalArgumentException("找不到優惠券：" + loyaltyCouponId));
            if (Boolean.TRUE.equals(coupon.getIsUsed())) {
                throw new IllegalStateException("此優惠券已使用過");
            }
            BigDecimal discounted;
            if (coupon.getDiscountAmount() != null) {
                discounted = original.subtract(coupon.getDiscountAmount()).max(BigDecimal.ZERO);
            } else {
                BigDecimal couponRate = coupon.getDiscountRate() != null
                        ? coupon.getDiscountRate()
                        : config.getLoyaltyRate();
                discounted = original.multiply(couponRate).setScale(2, RoundingMode.HALF_UP);
            }
            return new DiscountResult(DiscountType.COUPON, original, discounted, coupon);
        }

        // 4. 早鳥優惠
        if (requestedDiscount == DiscountType.EARLY_BIRD || isEarlyBirdActive(activity)) {
            if (isEarlyBirdActive(activity)) {
                BigDecimal discounted = original.multiply(config.getEarlyBirdRate())
                        .setScale(2, RoundingMode.HALF_UP);
                return new DiscountResult(DiscountType.EARLY_BIRD, original, discounted, null);
            }
        }

        // 5. 無折扣
        return new DiscountResult(DiscountType.NONE, original, original, null);
    }

    // =====================================================================
    //  忠誠優惠券發放（在 checkIn 後觸發）
    // =====================================================================

    /**
     * 檢查會員是否達到忠誠獎勵門檻，若是則自動發放新的優惠券。
     * 採冪等設計：totalAttended / threshold > totalCouponsEver issued → 補發差額
     */
    @Transactional
    public void checkAndAwardLoyaltyCoupons(String userId) {
        DiscountConfig config = getConfig();
        int threshold = config.getLoyaltyThreshold();

        // 計算已出席場次（ATTENDED）
        long attended = registrationRepository.countByUserIdAndStatus(userId, RegistrationStatus.ATTENDED);

        // 應獲得幾張券
        long shouldHave = attended / threshold;

        // 已發放幾張（含已使用）
        long alreadyIssued = loyaltyCouponRepository.countByUserId(userId);

        long toIssue = shouldHave - alreadyIssued;
        for (long i = 0; i < toIssue; i++) {
            issueCoupon(userId, "COUPON", null, "累積出席獎勵", "SYSTEM");
        }
    }

    // =====================================================================
    //  優惠券發放
    // =====================================================================

    /**
     * 通用發放優惠券
     */
    @Transactional
    public LoyaltyCoupon issueCoupon(String userId, String couponType,
                                      BigDecimal discountRate, String description, String issuedBy) {
        return issueCoupon(userId, couponType, discountRate, null, description, issuedBy);
    }

    @Transactional
    public LoyaltyCoupon issueCoupon(String userId, String couponType,
                                      BigDecimal discountRate, BigDecimal discountAmount,
                                      String description, String issuedBy) {
        LoyaltyCoupon coupon = new LoyaltyCoupon();
        coupon.setUserId(userId);
        coupon.setCouponType(couponType != null ? couponType : "CUSTOM");
        coupon.setDiscountRate(discountRate);
        coupon.setDiscountAmount(discountAmount);
        coupon.setDescription(description);
        coupon.setIssuedBy(issuedBy);
        return loyaltyCouponRepository.save(coupon);
    }

    /**
     * 新人優惠券：在使用者完成註冊後呼叫
     */
    @Transactional
    public void issueWelcomeCoupon(String userId) {
        DiscountConfig config = getConfig();
        Boolean enabled = config.getWelcomeCouponEnabled();
        if (enabled == null || !enabled) return;
        BigDecimal rate = config.getWelcomeCouponRate() != null
                ? config.getWelcomeCouponRate()
                : new BigDecimal("0.80");
        issueCoupon(userId, "WELCOME", rate, "新人優惠券", "SYSTEM");
    }

    /**
     * 批次發放優惠券給一組會員
     * @param targetGroup "OFFICERS" | "ALL_MEMBERS"
     * @param discountRate 折扣率
     * @param description 備注
     * @param issuedBy 發放者
     * @return 發放張數
     */
    @Transactional
    public int issueBatchCoupons(String targetGroup, BigDecimal discountRate, String description, String issuedBy) {
        List<User> targets;
        if ("OFFICERS".equalsIgnoreCase(targetGroup)) {
            targets = userRepository.findByRole("officer");
        } else if ("ALL_MEMBERS".equalsIgnoreCase(targetGroup)) {
            targets = userRepository.findByRole("member");
        } else {
            throw new IllegalArgumentException("未知的發放對象：" + targetGroup);
        }
        for (User user : targets) {
            issueCoupon(user.getId(), "CUSTOM", discountRate, description, issuedBy);
        }
        return targets.size();
    }

    /**
     * 管理員查詢所有優惠券（含使用者名稱）
     */
    public List<CouponAdminDTO> adminListAllCoupons() {
        List<LoyaltyCoupon> all = loyaltyCouponRepository.findAllByOrderByEarnedAtDesc();
        return all.stream().map(c -> {
            String userName = userRepository.findById(c.getUserId())
                    .map(User::getName).orElse("未知");
            CouponAdminDTO dto = new CouponAdminDTO();
            dto.setId(c.getId());
            dto.setUserId(c.getUserId());
            dto.setUserName(userName);
            dto.setCouponType(c.getCouponType());
            dto.setDiscountRate(c.getDiscountRate());
            dto.setDescription(c.getDescription());
            dto.setIsUsed(c.getIsUsed());
            dto.setEarnedAt(c.getEarnedAt());
            dto.setUsedAt(c.getUsedAt());
            dto.setIssuedBy(c.getIssuedBy());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 標記優惠券為已使用，並記錄用於哪筆報名。
     * 在 RegistrationService 確認折扣後呼叫。
     */
    @Transactional
    public void markCouponUsed(Long couponId, Long registrationId) {
        LoyaltyCoupon coupon = loyaltyCouponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("找不到優惠券：" + couponId));
        if (Boolean.TRUE.equals(coupon.getIsUsed())) {
            throw new IllegalStateException("此優惠券已使用過");
        }
        coupon.setIsUsed(true);
        coupon.setUsedAt(LocalDateTime.now());
        coupon.setUsedForRegistrationId(registrationId);
        loyaltyCouponRepository.save(coupon);
    }

    /**
     * 查詢某會員的所有未使用優惠券
     */
    public List<LoyaltyCoupon> getAvailableCoupons(String userId) {
        return loyaltyCouponRepository.findByUserIdAndIsUsedFalseOrderByEarnedAtAsc(userId);
    }

    /**
     * 查詢某會員的所有優惠券（含已使用）
     */
    public List<LoyaltyCoupon> getAllCoupons(String userId) {
        return loyaltyCouponRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    // =====================================================================
    //  Inner Result Class
    // =====================================================================

    /**
     * 折扣計算結果
     */
    public static class DiscountResult {
        public final DiscountType discountType;
        public final BigDecimal originalAmount;
        public final BigDecimal finalAmount;
        public final LoyaltyCoupon usedCoupon; // null if not loyalty

        public DiscountResult(DiscountType type, BigDecimal original, BigDecimal finalAmt, LoyaltyCoupon coupon) {
            this.discountType = type;
            this.originalAmount = original;
            this.finalAmount = finalAmt;
            this.usedCoupon = coupon;
        }
    }
}
