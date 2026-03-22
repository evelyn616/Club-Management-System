package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.CouponAdminDTO;
import com.danceclub.club_system.dto.DiscountConfigRequest;
import com.danceclub.club_system.dto.DiscountPreviewDTO;
import com.danceclub.club_system.dto.IssueCouponRequest;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.DiscountConfig;
import com.danceclub.club_system.model.LoyaltyCoupon;
import com.danceclub.club_system.service.ActivityService;
import com.danceclub.club_system.service.DiscountService;
import com.danceclub.club_system.service.UserService;
import com.danceclub.club_system.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 折扣相關 API
 *
 * GET  /api/discount/config                        → 取得全域折扣設定（管理員）
 * PUT  /api/discount/config                        → 更新全域折扣設定（管理員）
 * GET  /api/discount/preview?activityId=&userId=   → 報名前折扣預覽（會員 / 管理員）
 * GET  /api/discount/coupons/my?userId=            → 查詢我的優惠券（會員）
 * GET  /api/discount/coupons/all?userId=           → 查詢某會員所有券（管理員）
 */
@RestController
@RequestMapping("/api/discount")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DiscountController {

    private final DiscountService discountService;
    private final ActivityService activityService;
    private final UserService userService;

    public DiscountController(DiscountService discountService,
                              ActivityService activityService,
                              UserService userService) {
        this.discountService = discountService;
        this.activityService = activityService;
        this.userService = userService;
    }

    // =====================================================================
    //  全域折扣設定（管理員）
    // =====================================================================

    /**
     * 取得全域折扣設定
     * GET /api/discount/config
     */
    @GetMapping("/config")
    public ResponseEntity<DiscountConfig> getConfig() {
        return ResponseEntity.ok(discountService.getConfig());
    }

    /**
     * 更新全域折扣設定
     * PUT /api/discount/config
     * Body: { earlyBirdDefaultDays, earlyBirdRate, loyaltyThreshold, loyaltyRate }
     */
    @PutMapping("/config")
    public ResponseEntity<DiscountConfig> updateConfig(
            @Valid @RequestBody DiscountConfigRequest request,
            @RequestParam(defaultValue = "admin") String updatedBy
    ) {
        DiscountConfig updated = discountService.updateConfig(request, updatedBy);
        return ResponseEntity.ok(updated);
    }

    // =====================================================================
    //  報名前折扣預覽
    // =====================================================================

    /**
     * 報名前折扣預覽
     * GET /api/discount/preview?activityId=1&userId=m0001
     */
    @GetMapping("/preview")
    public ResponseEntity<?> preview(
            @RequestParam Long activityId,
            @RequestParam String userId
    ) {
        try {
            Activity activity = activityService.getActivityById(activityId);
            UserResponse user = userService.getUserById(userId);
            DiscountPreviewDTO preview = discountService.preview(activity, user.getRole(), userId);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // =====================================================================
    //  忠誠優惠券
    // =====================================================================

    /**
     * 查詢我的未使用優惠券
     * GET /api/discount/coupons/my?userId=m0001
     */
    @GetMapping("/coupons/my")
    public ResponseEntity<List<LoyaltyCoupon>> getMyCoupons(@RequestParam String userId) {
        return ResponseEntity.ok(discountService.getAvailableCoupons(userId));
    }

    /**
     * 查詢某會員所有優惠券（含已使用）— 管理員用
     * GET /api/discount/coupons/all?userId=m0001
     */
    @GetMapping("/coupons/all")
    public ResponseEntity<List<LoyaltyCoupon>> getAllCoupons(@RequestParam String userId) {
        return ResponseEntity.ok(discountService.getAllCoupons(userId));
    }

    /**
     * 管理員手動發放優惠券給指定會員
     * POST /api/discount/coupons/issue
     * Body: { userId, discountRate, description }
     */
    @PostMapping("/coupons/issue")
    public ResponseEntity<?> issueCoupon(
            @Valid @RequestBody IssueCouponRequest request,
            @RequestParam(defaultValue = "admin") String issuedBy
    ) {
        try {
            // validate user exists
            userService.getUserById(request.getUserId());
            var coupon = discountService.issueCoupon(
                    request.getUserId(), "CUSTOM",
                    request.getDiscountRate(), request.getDiscountAmount(),
                    request.getDescription(), issuedBy
            );
            return ResponseEntity.ok(coupon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 批次發放優惠券給幹部或全部會員
     * POST /api/discount/coupons/issue-batch?targetGroup=OFFICERS&issuedBy=m0001
     * Body: { discountRate, description }
     */
    @PostMapping("/coupons/issue-batch")
    public ResponseEntity<?> issueBatchCoupons(
            @RequestParam String targetGroup,
            @RequestParam(defaultValue = "admin") String issuedBy,
            @RequestBody Map<String, Object> body
    ) {
        try {
            BigDecimal rate = new BigDecimal(body.get("discountRate").toString());
            String description = body.containsKey("description") ? (String) body.get("description") : null;
            int count = discountService.issueBatchCoupons(targetGroup, rate, description, issuedBy);
            return ResponseEntity.ok(Map.of("count", count, "message", "已成功發放 " + count + " 張優惠券"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 管理員查詢所有優惠券
     * GET /api/discount/coupons/admin/all
     */
    @GetMapping("/coupons/admin/all")
    public ResponseEntity<List<CouponAdminDTO>> adminListAllCoupons() {
        return ResponseEntity.ok(discountService.adminListAllCoupons());
    }
}
