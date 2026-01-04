package com.danceclub.club_system.model;


import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_registration")
public class Registration {

    //基本資料

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "活動id不可為空")
    @Column(nullable = false, name = "activity_id")
    private Long activityId;

    @NotNull(message = "用戶id不可為空")
    @Column(nullable = false, name = "user_id")
    private String userId;

    //====狀態設定====//

    @NotNull(message = "報名狀態不可為空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    //====報名時間====//
    @NotNull(message = "報名時間不可為空")
    @Column(nullable = false, name = "registration_time")
    private LocalDateTime registrationTime;

    //====備註====//
    @Size(max = 200, message = "備註最多200字")
    @Column(nullable = true, length = 200)
    private String note;

    //====繳費====//
    @NotNull(message = "繳費狀態不得為空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "payment_status")
    private PaymentStatus paymentStatus;

    //金額//
    @NotNull(message = "繳費金額不得為空")
    @DecimalMin(value = "0.0", inclusive = true, message = "費用不得為負數")
    @Column(name = "payment_amount", nullable = false ,precision = 10,scale = 2)
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    //====簽到====//
    //是否簽到//
    @NotNull(message = "簽到狀態不得為空")
    @Column(name = "checked_in", nullable = false)
    public Boolean checkedIn = false;

    //簽到時間//
    @Column(name = "check_in_time", nullable = true)
    private LocalDateTime checkInTime;

    //判斷是否遲到//
    @Column(name = "is_late",nullable = true)
    public Boolean isLate;

    //====創建時間====//
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    //====更新時間====//
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //====確認簽到狀態以及能否取消====//
    public boolean isCheckedIn(){
        return Boolean.TRUE.equals(checkedIn);
    }
    public boolean canCancel() {
        return RegistrationStatus.REGISTERED.equals(status) && !isCheckedIn();
    }

    //====是否繳費完成====//
    public boolean isPaid(){
        return PaymentStatus.PAID.equals(paymentStatus);
    }

    //====是否報名不用錢====//
    public boolean isFree(){
        return PaymentStatus.NOT_REQUIRED.equals(paymentStatus);
    }

    //====判斷是否需要繳費====//
    public boolean requiresPayment(){
        return PaymentStatus.PENDING.equals(paymentStatus);
                    }




    //====JPA生命週期方法====//
    @PrePersist
    protected void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        //設定預設值
        if (registrationTime == null){
            registrationTime = now;
        }
        if (status == null){
            status = RegistrationStatus.REGISTERED;
        }
        if (paymentStatus == null){
            paymentStatus = PaymentStatus.PENDING;
        }
        if (paymentAmount == null){
            paymentAmount = BigDecimal.ZERO;
        }
        if (checkedIn == null){
            checkedIn = false;
        }
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }

}
