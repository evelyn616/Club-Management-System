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

import java.time.LocalDateTime;
import java.util.HashMap;
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
     * 
     * 當用戶完成付款後，綠界會將用戶導向此頁面
     * 我們在這裡處理付款成功的狀態更新
     */
    @PostMapping("/ecpay/return")
    public ResponseEntity<String> handleEcpayReturn(@RequestParam Map<String, String> params) {
        try {
            // 驗證 CheckMacValue
            String receivedCheckMacValue = params.get("CheckMacValue");
            Map<String, String> paramsForValidation = new HashMap<>(params);
            paramsForValidation.remove("CheckMacValue");
            
            // 取得付款資訊
            String merchantTradeNo = params.get("MerchantTradeNo");
            String rtnCode = params.get("RtnCode");
            String rtnMsg = params.get("RtnMsg");
            String tradeNo = params.get("TradeNo");
            String tradeAmt = params.get("TradeAmt");
            String paymentDate = params.get("PaymentDate");
            String paymentType = params.get("PaymentType");
            String paymentTypeChargeFee = params.get("PaymentTypeChargeFee");
            String tradeDate = params.get("TradeDate");
            String simulatePaid = params.get("SimulatePaid");
            
            // 記錄日誌
            System.out.println("=== 綠界付款返回 ===");
            System.out.println("訂單編號: " + merchantTradeNo);
            System.out.println("返回代碼: " + rtnCode);
            System.out.println("返回訊息: " + rtnMsg);
            System.out.println("綠界交易編號: " + tradeNo);
            System.out.println("付款金額: " + tradeAmt);
            System.out.println("付款時間: " + paymentDate);
            System.out.println("付款方式: " + paymentType);
            System.out.println("是否為模擬付款: " + simulatePaid);
            
            // 如果付款成功，更新資料庫狀態
            if ("1".equals(rtnCode)) {
                try {
                    // 從訂單編號解析 Payment ID
                    Long paymentId = extractPaymentIdFromMerchantTradeNo(merchantTradeNo);
                    
                    // 更新付款狀態
                    Payment payment = paymentService.getPaymentById(paymentId);
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setEcpayTradeNo(tradeNo);
                    payment.setEcpayResponse(params.toString());
                    
                    // 處理不同付款方式的額外資訊
                    if (paymentType != null) {
                        if (paymentType.contains("ATM")) {
                            payment.setAtmBankCode(params.get("BankCode"));
                            payment.setAtmAccount(params.get("vAccount"));
                        } else if (paymentType.contains("CVS") || paymentType.contains("BARCODE")) {
                            payment.setCvsPaymentCode(params.get("PaymentNo"));
                            payment.setCvsType(params.get("PaymentType"));
                        }
                    }
                    
                    // 添加備註
                    String notePrefix = "1".equals(simulatePaid) ? "[模擬付款] " : "";
                    payment.setNote(notePrefix + "付款成功 - 綠界交易編號: " + tradeNo + 
                                   ", 付款時間: " + paymentDate + 
                                   ", 付款方式: " + paymentType);
                    
                    paymentService.updatePayment(payment);
                    
                    System.out.println("✓ 付款狀態已更新: Payment ID=" + paymentId);
                    
                    // 返回成功頁面（HTML）
                    return ResponseEntity.ok()
                            .contentType(org.springframework.http.MediaType.TEXT_HTML)
                            .body(generateSuccessHtml(merchantTradeNo, tradeAmt, paymentDate));
                    
                } catch (Exception e) {
                    System.err.println("✗ 更新付款狀態失敗: " + e.getMessage());
                    e.printStackTrace();
                    // 即使更新失敗，也顯示付款成功訊息（因為綠界端已經成功）
                    return ResponseEntity.ok()
                            .contentType(org.springframework.http.MediaType.TEXT_HTML)
                            .body(generateSuccessHtml(merchantTradeNo, tradeAmt, paymentDate));
                }
            } else {
                // 付款失敗
                System.out.println("✗ 付款失敗: " + rtnMsg);
                
                // 嘗試更新失敗原因
                try {
                    Long paymentId = extractPaymentIdFromMerchantTradeNo(merchantTradeNo);
                    Payment payment = paymentService.getPaymentById(paymentId);
                    payment.setFailureReason("付款失敗 - 代碼: " + rtnCode + ", 訊息: " + rtnMsg);
                    payment.setEcpayResponse(params.toString());
                    paymentService.updatePayment(payment);
                } catch (Exception e) {
                    System.err.println("✗ 更新失敗原因時發生錯誤: " + e.getMessage());
                }
                
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.TEXT_HTML)
                        .body(generateFailureHtml(merchantTradeNo, rtnMsg));
            }
        } catch (Exception e) {
            System.err.println("✗ 處理付款返回時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(generateErrorHtml(e.getMessage()));
        }
    }
    
    /**
     * 從訂單編號解析 Payment ID
     */
    private Long extractPaymentIdFromMerchantTradeNo(String merchantTradeNo) {
        // 格式: P{paymentId}{timestamp}
        if (merchantTradeNo != null && merchantTradeNo.startsWith("P") && merchantTradeNo.length() > 15) {
            String withoutPrefix = merchantTradeNo.substring(1);
            String paymentIdStr = withoutPrefix.substring(0, withoutPrefix.length() - 14);
            return Long.parseLong(paymentIdStr);
        }
        throw new IllegalArgumentException("Invalid MerchantTradeNo format: " + merchantTradeNo);
    }
    
    /**
     * 生成付款成功的 HTML 頁面
     */
    private String generateSuccessHtml(String merchantTradeNo, String amount, String paymentDate) {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>付款成功</title>
                <style>
                    body {
                        font-family: 'Microsoft JhengHei', Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                    }
                    .success-icon {
                        font-size: 64px;
                        color: #4CAF50;
                        margin-bottom: 20px;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    .info {
                        background: #f5f5f5;
                        padding: 20px;
                        border-radius: 5px;
                        margin: 20px 0;
                        text-align: left;
                    }
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        margin: 10px 0;
                        padding: 5px 0;
                        border-bottom: 1px solid #ddd;
                    }
                    .info-label {
                        color: #666;
                        font-weight: bold;
                    }
                    .info-value {
                        color: #333;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: #667eea;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin-top: 20px;
                        transition: background 0.3s;
                    }
                    .button:hover {
                        background: #5568d3;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✓</div>
                    <h1>付款成功！</h1>
                    <p>您的付款已經完成，感謝您的支持。</p>
                    
                    <div class="info">
                        <div class="info-row">
                            <span class="info-label">訂單編號：</span>
                            <span class="info-value">""" + merchantTradeNo + """
</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">付款金額：</span>
                            <span class="info-value">NT$ """ + amount + """
</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">付款時間：</span>
                            <span class="info-value">""" + paymentDate + """
</span>
                        </div>
                    </div>
                    
                    <a href="http://localhost:5173/dashboard" class="button">返回系統</a>
                </div>
            </body>
            </html>
            """;
    }
    
    /**
     * 生成付款失敗的 HTML 頁面
     */
    private String generateFailureHtml(String merchantTradeNo, String errorMessage) {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>付款失敗</title>
                <style>
                    body {
                        font-family: 'Microsoft JhengHei', Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                    }
                    .error-icon {
                        font-size: 64px;
                        color: #f44336;
                        margin-bottom: 20px;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    .error-message {
                        background: #ffebee;
                        color: #c62828;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: #f44336;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin-top: 20px;
                        transition: background 0.3s;
                    }
                    .button:hover {
                        background: #d32f2f;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">✗</div>
                    <h1>付款失敗</h1>
                    <p>很抱歉，您的付款未能完成。</p>
                    
                    <div class="error-message">
                        <strong>錯誤訊息：</strong><br>
                        """ + errorMessage + """
                    </div>
                    
                    <p>訂單編號：""" + merchantTradeNo + """
</p>
                    
                    <a href="http://localhost:5173/payments/pending" class="button">重新付款</a>
                </div>
            </body>
            </html>
            """;
    }
    
    /**
     * 生成錯誤頁面
     */
    private String generateErrorHtml(String errorMessage) {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>系統錯誤</title>
                <style>
                    body {
                        font-family: 'Microsoft JhengHei', Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: #f5f5f5;
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                    }
                    .error-icon {
                        font-size: 64px;
                        color: #ff9800;
                        margin-bottom: 20px;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    .error-message {
                        background: #fff3e0;
                        color: #e65100;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                        word-break: break-word;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: #ff9800;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin-top: 20px;
                        transition: background 0.3s;
                    }
                    .button:hover {
                        background: #f57c00;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">⚠</div>
                    <h1>系統錯誤</h1>
                    <p>處理付款返回時發生錯誤。</p>
                    
                    <div class="error-message">
                        """ + errorMessage + """
                    </div>
                    
                    <a href="http://localhost:5173/dashboard" class="button">返回系統</a>
                </div>
            </body>
            </html>
            """;
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
     * 選擇現金付款
     * POST /api/payments/{id}/cash
     */
    @PostMapping("/{id}/cash")
    public ResponseEntity<?> selectCashPayment(@PathVariable Long id, 
                                               @RequestBody(required = false) CashPaymentRequest request) {
        try {
            String note = request != null ? request.getNote() : null;
            Payment payment = paymentService.selectCashPayment(id, note);
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CASH_PAYMENT_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "選擇現金付款時發生錯誤"));
        }
    }

    /**
     * 審核現金付款（管理員）
     * PUT /api/payments/{id}/approve-cash
     */
    @PutMapping("/{id}/approve-cash")
    public ResponseEntity<?> approveCashPayment(@PathVariable Long id, 
                                               @RequestBody Map<String, String> body) {
        try {
            String adminId = getCurrentUserId();
            String reviewNote = body.get("reviewNote");
            
            Payment payment = paymentService.approveCashPayment(id, adminId, reviewNote);
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "APPROVE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "審核現金付款時發生錯誤"));
        }
    }

    /**
     * 拒絕現金付款（管理員）
     * PUT /api/payments/{id}/reject-cash
     */
    @PutMapping("/{id}/reject-cash")
    public ResponseEntity<?> rejectCashPayment(@PathVariable Long id, 
                                              @RequestBody Map<String, String> body) {
        try {
            String adminId = getCurrentUserId();
            String reason = body.getOrDefault("reason", "未通過審核");
            
            Payment payment = paymentService.rejectCashPayment(id, adminId, reason);
            PaymentResponse response = convertToResponse(payment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "REJECT_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "拒絕現金付款時發生錯誤"));
        }
    }

    /**
     * 取得待審核的現金付款列表（管理員）
     * GET /api/payments/admin/pending-review
     */
    @GetMapping("/admin/pending-review")
    public ResponseEntity<?> getPendingReviewPayments() {
        try {
            List<Payment> payments = paymentService.getPendingReviewPayments();
            List<PaymentResponse> responses = payments.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "取得待審核列表時發生錯誤"));
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
