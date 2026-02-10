package com.danceclub.club_system.controller;

import com.danceclub.club_system.dto.*;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.User;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.repository.UserRepository;
import com.danceclub.club_system.service.EcpayService;
import com.danceclub.club_system.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Controller - 處理付款相關請求
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final EcpayService ecpayService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(EcpayService ecpayService, PaymentService paymentService, UserRepository userRepository) {
        this.ecpayService = ecpayService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    /**
     * 從 Authentication 取得當前用戶 ID
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // JWT 的 subject 是 email
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        
        return user.getId();
    }

    /**
     * 創建繳費記錄
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        try {
            Payment payment = paymentService.createPayment(
                request.getRegistrationId(),
                request.getPaymentType(),
                request.getAmount(),
                request.getOriginalAmount()
            );
            
            if (request.getDiscountReason() != null) {
                payment.setDiscountReason(request.getDiscountReason());
            }
            
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CREATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "創建繳費記錄時發生錯誤"));
        }
    }

    /**
     * 取得繳費記錄
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得繳費記錄時發生錯誤"));
        }
    }

    /**
     * 取得當前用戶的所有繳費記錄
     * GET /api/payments/my
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments() {
        try {
            String userId = getCurrentUserId();
            
            List<Payment> payments = paymentService.getUserPayments(userId);
            List<PaymentResponse> responses = payments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得繳費記錄時發生錯誤"));
        }
    }

    /**
     * 取得當前用戶的待繳費記錄
     * GET /api/payments/my/pending
     */
    @GetMapping("/my/pending")
    public ResponseEntity<?> getMyPendingPayments() {
        try {
            String userId = getCurrentUserId();
            
            List<Payment> payments = paymentService.getUserPendingPayments(userId);
            List<PaymentResponse> responses = payments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得待繳費記錄時發生錯誤"));
        }
    }

    /**
     * 更新轉帳後五碼
     * PUT /api/payments/{id}/bank-proof
     */
    @PutMapping("/{id}/bank-proof")
    public ResponseEntity<?> updateBankProof(@PathVariable Long id, 
                                            @Valid @RequestBody UpdateBankProofRequest request) {
        try {
            Payment payment = paymentService.updateBankAccountProof(id, request.getBankAccountProof());
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "UPDATE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "更新轉帳資訊時發生錯誤"));
        }
    }

    /**
     * 審核繳費（管理員）
     * PUT /api/payments/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approvePayment(@PathVariable Long id, 
                                           @Valid @RequestBody ApprovePaymentRequest request) {
        try {
            String adminId = getCurrentUserId();
            
            Payment payment = paymentService.markAsPaid(
                id, 
                request.getMethod(), 
                adminId, 
                request.getReviewNote()
            );
            
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "APPROVE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "審核繳費時發生錯誤"));
        }
    }

    /**
     * 取消繳費
     * PUT /api/payments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long id, 
                                          @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "用戶取消");
            Payment payment = paymentService.cancelPayment(id, reason);
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CANCEL_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取消繳費時發生錯誤"));
        }
    }

    /**
     * 創建綠界結帳訂單
     * POST /api/payments/ecpay/checkout
     */
    @PostMapping("/ecpay/checkout")
    public ResponseEntity<?> createEcpayCheckout(@RequestBody EcpayCheckoutRequest request) {
        try {
            EcpayCheckoutResponse response = ecpayService.createCheckout(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CHECKOUT_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "結帳過程中發生錯誤"));
        }
    }

    /**
     * 綠界付款通知（Notify）
     * POST /api/payments/ecpay/notify
     */
    @PostMapping("/ecpay/notify")
    public ResponseEntity<String> handleEcpayNotify(@RequestParam Map<String, String> params) {
        try {
            String result = ecpayService.handlePaymentNotify(params);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("0|" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("0|Internal error");
        }
    }

    /**
     * 綠界付款返回（Return）
     * POST /api/payments/ecpay/return
     */
    @PostMapping("/ecpay/return")
    public ResponseEntity<String> handleEcpayReturn(@RequestParam Map<String, String> params) {
        try {
            String merchantTradeNo = params.get("MerchantTradeNo");
            String rtnCode = params.get("RtnCode");
            
            if ("1".equals(rtnCode)) {
                return ResponseEntity.ok("付款成功！訂單編號：" + merchantTradeNo);
            } else {
                return ResponseEntity.ok("付款失敗！訂單編號：" + merchantTradeNo);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("處理返回時發生錯誤");
        }
    }

    /**
     * 取得所有待審核的現金繳費（管理員）
     * GET /api/payments/admin/pending-cash
     */
    @GetMapping("/admin/pending-cash")
    public ResponseEntity<?> getPendingCashPayments() {
        try {
            List<Payment> payments = paymentService.getPendingCashPayments();
            List<PaymentResponse> responses = payments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得待審核繳費時發生錯誤"));
        }
    }

    /**
     * 取得統計資訊（管理員）
     * GET /api/payments/admin/statistics
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> stats = Map.of(
                "totalPaid", paymentService.getTotalAmountByStatus(PaymentStatus.PAID),
                "totalPending", paymentService.getTotalAmountByStatus(PaymentStatus.PENDING),
                "countPaid", paymentService.countByStatus(PaymentStatus.PAID),
                "countPending", paymentService.countByStatus(PaymentStatus.PENDING),
                "countCancelled", paymentService.countByStatus(PaymentStatus.CANCELLED)
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得統計資訊時發生錯誤"));
        }
    }

    /**
     * 轉換 Payment 為 PaymentResponse
     */
    private PaymentResponse convertToResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getRegistration() != null ? payment.getRegistration().getId() : null,
            payment.getPaymentType(),
            payment.getOriginalAmount(),
            payment.getAmount(),
            payment.getDiscountAmount(),
            payment.getDiscountReason(),
            payment.getStatus(),
            payment.getMethod(),
            payment.getPaidAt(),
            payment.getReviewedBy(),
            payment.getReviewedAt(),
            payment.getReviewNote(),
            payment.getNote(),
            payment.getBankAccountProof(),
            payment.getMerchantTradeNo(),
            payment.getEcpayTradeNo(),
            payment.getPaymentDeadline(),
            payment.getAtmAccount(),
            payment.getAtmBankCode(),
            payment.getCvsPaymentCode(),
            payment.getCvsType(),
            payment.getFailureReason(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
