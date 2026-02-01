package com.danceclub.club_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 請假申請實體
 */
@Entity
@Table(name = "leave_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 1. 提交請假的人 - 改為 user 並對齊對方的 id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * 2. 關聯的活動
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity; 

    @Column(nullable = false)
    private String reason;

    @Column(name = "leave_type", nullable = false)
    private String leaveType;

    /**
     * 預設狀態為 PENDING
     * 加上 @Builder.Default 以確保 Lombok Builder 模式下預設值生效
     */
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * 3. 審核人 - 保持與 User 實體一致
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== 生命週期回調 ==========

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}