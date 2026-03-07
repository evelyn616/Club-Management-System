package com.danceclub.club_system.service;

import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentMethod;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;
import com.danceclub.club_system.repository.PaymentRepository;
import com.danceclub.club_system.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Service - 處理繳費相關業務邏輯
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RegistrationRepository registrationRepository;

    public PaymentService(PaymentRepository paymentRepository, 
                         RegistrationRepository registrationRepository) {
        this.paymentRepository = paymentRepository;
        this.registrationRepository = registrationRepository;
    }

    /**
     * 創建繳費記錄
     */
    @Transactional
    public Payment createPayment(Long registrationId, PaymentType paymentType, 
                                 BigDecimal amount, BigDecimal originalAmount) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("報名記錄不存在"));

        Payment payment = new Payment();
        payment.setRegistration(registration);
        payment.setPaymentType(paymentType);
        payment.setOriginalAmount(originalAmount);
        payment.setAmount(amount);
        payment.setDiscountAmount(originalAmount.subtract(amount));
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    /**
     * 取得繳費記錄
     */
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("繳費記錄不存在"));
    }

    /**
     * 取得用戶的所有繳費記錄
     */
    public List<Payment> getUserPayments(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * 取得用戶的待繳費記錄
     */
    public List<Payment> getUserPendingPayments(String userId) {
        return paymentRepository.findByUserIdAndStatus(userId, PaymentStatus.PENDING);
    }

    /**
     * 更新繳費狀態為已付款（現金/轉帳）
     */
    @Transactional
    public Payment markAsPaid(Long paymentId, PaymentMethod method, 
                             String reviewedBy, String reviewNote) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("只能審核待付款的記錄");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod(method);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReviewedBy(reviewedBy);
        payment.setReviewedAt(LocalDateTime.now());
        payment.setReviewNote(reviewNote);

        return paymentRepository.save(payment);
    }

    /**
     * 更新轉帳後五碼
     */
    @Transactional
    public Payment updateBankAccountProof(Long paymentId, Long bankAccountProof) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("只能更新待付款記錄的轉帳資訊");
        }

        payment.setBankAccountProof(bankAccountProof);
        payment.setMethod(PaymentMethod.BANK_TRANSFER);

        return paymentRepository.save(payment);
    }

    /**
     * 取消繳費
     */
    @Transactional
    public Payment cancelPayment(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("已付款的記錄不能取消，請使用退款功能");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);

        return paymentRepository.save(payment);
    }

    /**
     * 申請退款
     */
    @Transactional
    public Payment requestRefund(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("只能對已付款的記錄申請退款");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setNote("退款原因：" + reason);

        return paymentRepository.save(payment);
    }

    /**
     * 審核退款
     */
    @Transactional
    public Payment approveRefund(Long paymentId, String reviewedBy, String reviewNote) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("只能審核退款中的記錄");
        }

        payment.setReviewedBy(reviewedBy);
        payment.setReviewedAt(LocalDateTime.now());
        payment.setReviewNote(reviewNote);

        return paymentRepository.save(payment);
    }

    /**
     * 取得所有待審核的現金繳費
     */
    public List<Payment> getPendingCashPayments() {
        return paymentRepository.findPendingCashPayments();
    }

    /**
     * 取得所有退款記錄
     */
    public List<Payment> getRefundedPayments() {
        return paymentRepository.findRefundedPayments();
    }

    /**
     * 取得逾期未繳費的記錄
     */
    public List<Payment> getExpiredPayments() {
        return paymentRepository.findExpiredPendingPayments();
    }

    /**
     * 根據轉帳後五碼查詢繳費記錄
     */
    public List<Payment> getPaymentsByBankProof(Long bankAccountProof) {
        return paymentRepository.findByBankAccountProof(bankAccountProof);
    }

    /**
     * 根據商店訂單編號查詢繳費記錄
     */
    public Payment getPaymentByMerchantTradeNo(String merchantTradeNo) {
        return paymentRepository.findByMerchantTradeNo(merchantTradeNo)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單編號的繳費記錄"));
    }

    /**
     * 計算指定狀態的總金額
     */
    public BigDecimal getTotalAmountByStatus(PaymentStatus status) {
        BigDecimal total = paymentRepository.sumAmountByStatus(status);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 統計指定狀態的繳費數量
     */
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    /**
     * 更新繳費記錄
     */
    @Transactional
    public Payment updatePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
