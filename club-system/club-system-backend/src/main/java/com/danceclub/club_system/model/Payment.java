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
    @JoinColumn(name = "registration_id", nullable = false)
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
    @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
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
    @Column(name = "status", nullable = false)
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
     * 創建時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
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
