package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.PromoCodeRequest;
import com.danceclub.club_system.dto.PromoCodeValidateDTO;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.PromoCode;
import com.danceclub.club_system.service.ActivityService;
import com.danceclub.club_system.service.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 優惠碼 API
 *
 * POST   /api/promo-codes               管理員建立優惠碼
 * GET    /api/promo-codes               管理員查詢所有優惠碼
 * DELETE /api/promo-codes/{id}          管理員刪除優惠碼
 * PUT    /api/promo-codes/{id}/toggle   管理員啟用/停用
 * GET    /api/promo-codes/validate      會員驗證優惠碼（預覽折扣，不計入使用次數）
 */
@RestController
@RequestMapping("/api/promo-codes")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    private final ActivityService activityService;

    public PromoCodeController(PromoCodeService promoCodeService, ActivityService activityService) {
        this.promoCodeService = promoCodeService;
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody PromoCodeRequest request,
            @RequestParam(defaultValue = "admin") String createdBy
    ) {
        try {
            PromoCode promo = promoCodeService.createPromoCode(request, createdBy);
            return ResponseEntity.ok(promo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PromoCode>> listAll() {
        return ResponseEntity.ok(promoCodeService.listAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            promoCodeService.deletePromoCode(id);
            return ResponseEntity.ok(Map.of("message", "已刪除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        try {
            PromoCode updated = promoCodeService.toggleActive(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<PromoCodeValidateDTO> validate(
            @RequestParam String code,
            @RequestParam Long activityId
    ) {
        Activity activity = activityService.getActivityById(activityId);
        PromoCodeValidateDTO result = promoCodeService.validate(code, activity.getFeeAmount());
        return ResponseEntity.ok(result);
    }
}
