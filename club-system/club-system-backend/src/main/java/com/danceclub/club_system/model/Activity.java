package com.danceclub.club_system.model;

import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.ActivityType;
import com.danceclub.club_system.model.enums.TargetAudience;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 活動實體
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity")
public class Activity {

    // ========== 基本資訊 ==========

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "活動名稱不可為空")
    @Size(max = 100, message = "活動名稱最多 100 字")
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 5000, message = "活動說明最多 5000 字")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 500, message = "圖片 URL 最多 500 字")
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    // ========== 時間地點 ==========

    @NotNull(message = "開始時間不可為空")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "結束時間不可為空")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Size(max = 200, message = "地點最多 200 字")
    @Column(length = 200)
    private String location;


    // ========== 報名設定 ==========
    @Min(value = 1, message = "人數上限至少為 1")
    @Column(name = "max_participants")
    private Integer maxParticipants;

    @NotNull(message = "報名截止期限不可為空")
    @Column(name = "registration_deadline",nullable = false)
    private LocalDateTime registrationDeadline;

    // ========== 費用 ==========

    @NotNull(message = "費用金額不可為空")
    @DecimalMin(value = "0.0", inclusive = true, message = "費用不可為負數")
    @Column(name = "fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    // ========== 活動類型 ==========

    @NotNull(message = "活動類型不得為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type",nullable = false, length = 50)
    private ActivityType activityType = ActivityType.REGULAR;

    // ========== 狀態 ==========

    @NotNull(message = "活動狀態不得為空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status = ActivityStatus.DRAFT;

    // ========== 發布時間 ==========

    @Column(name = "published_at", nullable = true, updatable = false)
    private LocalDateTime publishedAt;

    // ========== 參加對象 ==========
    @NotNull(message = "參加對象不得為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience",nullable = false,length = 50)
    private TargetAudience targetAudience = TargetAudience.ALL;

    // ========== 建立者 ==========

    @NotNull(message = "建立者不得為空")
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;


    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    // ========== 時間戳記 ==========

    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    // ========== JPA 生命週期回調(Entity不同狀態) ==========

    @PrePersist
    protected void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        //預設值
        if (feeAmount == null){
            feeAmount = BigDecimal.ZERO;
        }
        if (status == null){
            status = ActivityStatus.DRAFT;
        }
        if (activityType == null){
            activityType = ActivityType.REGULAR;
        }
        if (targetAudience == null){
            targetAudience = TargetAudience.ALL;
        }

    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
    // @PostPersist: 在 INSERT 之後執行
    @PostPersist
    protected void afterCreate() {
        System.out.println("新活動建立: " + id);
    }

    // @PreRemove: 在 DELETE 之前執行
    @PreRemove
    protected void beforeDelete() {
        System.out.println("即將刪除活動: " + id);
    }

    /**
     * 判斷活動是否需要繳費
     */
    public boolean requiresPayment(){
        return feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) >0;
    }
    /**
     * 判斷活動是否免費
     */
    public  boolean isFree(){
        return feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判斷活動是否已經開始
     */
    public  boolean hasStarted(){
        return LocalDateTime.now().isAfter(startTime);
    }
    /**
     * 判斷活動是否已經結束
     */
    public boolean hasEnded(){
        return LocalDateTime.now().isAfter(endTime);
    }
    /**
     * 判斷活動是否進行中
     */
    public boolean isOngoing(){
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime)&& now.isBefore(endTime);
    }
    /**
     * 判斷報名是否截止
     */
    public boolean isRegistrationClosed(){
        if (registrationDeadline ==null){
            return false;
        }
        return LocalDateTime.now().isAfter(registrationDeadline);
    }
    /**
     * 判斷活動是否已經發布
     */
    public boolean isPublished(){
        return ActivityStatus.PUBLISHED.equals(status);
    }
    /**
     * 判斷活動是否可以報名
     * (已發布 且 報名未截止 且 活動未開始)
     */
    public boolean canRegister(){
        return isPublished() && !isRegistrationClosed() && !hasStarted();
    }
    /**
     * 判斷是否有人數限制
     */
    public boolean hasParticipantLimit(){
        return  maxParticipants != null && maxParticipants > 0;
    }

    /**
     * 取得時間是否有效
     */
    @AssertTrue(message = "結束時間必須晚於開始時間")
    public boolean isValidTimeRange(){
        if (startTime == null || endTime == null){
            return true;
        }
        return endTime.isAfter(startTime);
    }



}

