package com.club.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 繳費 ID（主鍵），格式：P0001, P0002, P0003...
     */
    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String id;

    /**
     * 報名 ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private ActivityRegistration registration;

    /**
     * 付款金額
     */
    @Column(name = "amount", nullable = false)
    private Integer amount;

    /**
     * 付款方式（'cash', 'online'）
     */
    @Column(name = "method", length = 20, nullable = false)
    private String method;

    /**
     * 繳費狀態（'pending', 'paid', 'approved', 'refund_pending', 'refunded'）
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    /**
     * 付款時間（可為 NULL）
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * 審核管理員 ID（可為 NULL）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /**
     * 審核時間（可為 NULL）
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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
    }

    /**
     * 在更新之前自動設置更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
