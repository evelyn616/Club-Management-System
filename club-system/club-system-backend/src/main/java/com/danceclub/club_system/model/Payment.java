package com.danceclub.club_system.model;

import com.danceclub.club_system.model.enums.PaymentMethod;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - 繳費實體
 * 用於存儲繳費記錄和退費信息
 */
@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /**
     * 繳費 ID（主鍵）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 報名 ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Registration registration;

    /**
     * 費用類型（ACTIVITY_FEE/MEMBERSHIP_FEE/MATERIAL_FEE/ANNUAL_FEE/OTHER）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    /**
     * 原價（從 activity.fee_amount 複製）
     */
    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount;

    /**
     * 實際應繳金額 (original_amount - discount_amount)
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 折扣金額
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    /**
     * 折扣原因
     */
    @Column(name = "discount_reason")
    private String discountReason;

    /**
     * 繳費狀態（PENDING/PAID/CANCELLED/REFUNDED/PARTIAL_REFUNDED）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * 付款方式（CASH/BANK_TRANSFER/CREDIT_CARD/LINE_PAY/OTHER）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private PaymentMethod method;

    /**
     * 實際付款時間
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * 審核管理員 ID
     */
    @Column(name = "reviewed_by")
    private String reviewedBy;

    /**
     * 審核時間
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 審核備註
     */
    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    /**
     * 備註
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * 轉帳後五碼
     */
    @Column(name = "bankaccount_proof")
    private Long bankAccountProof;

    /**
     * 商店訂單編號（綠界）
     */
    @Column(name = "merchant_trade_no", length = 50)
    private String merchantTradeNo;

    /**
     * 綠界交易編號
     */
    @Column(name = "ecpay_trade_no", length = 50)
    private String ecpayTradeNo;

    /**
     * 付款期限（ATM/超商繳費）
     */
    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    /**
     * ATM 虛擬帳號
     */
    @Column(name = "atm_account", length = 50)
    private String atmAccount;

    /**
     * ATM 銀行代碼
     */
    @Column(name = "atm_bank_code", length = 10)
    private String atmBankCode;

    /**
     * 超商繳費代碼
     */
    @Column(name = "cvs_payment_code", length = 50)
    private String cvsPaymentCode;

    /**
     * 超商類型（7-11/FamilyMart/OK/HiLife）
     */
    @Column(name = "cvs_type", length = 20)
    private String cvsType;

    /**
     * 失敗原因
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    /**
     * 綠界完整回應（JSON 格式）
     */
    @Column(name = "ecpay_response", columnDefinition = "TEXT")
    private String ecpayResponse;

    /**
     * 創建時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 在持久化之前自動設置創建時間和更新時間
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        // 設定預設值
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
    }

    /**
     * 在更新之前自動設置更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
