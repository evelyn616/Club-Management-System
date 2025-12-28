package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    //====基本查詢====//

    //1.查詢某會員的所有的報名紀錄，按照報名時間做降序排列
    List<Registration>findByUserIdOrderByRegistrationTimeDesc(
            String userId
    );
    //2.查詢某會員對某活動的報名紀錄(檢查是否報名成功)
    Optional<Registration> findByActivityIdAndUserId(
            Long activityId,
            String userId
    );

    //(only管理員端)
    //3.查詢某個活動的報名紀錄，按照報名時間做生續排列
    List<Registration>findByActivityIdOrderByRegistrationTimeAsc(
            Long activityId
    );

    //====狀態查詢====//

    //4.查詢某個會員的特定狀態報名紀錄
    List<Registration>findByUserIdAndStatus(
            String userId,
            RegistrationStatus status
    );

    //(only管理員端)
    //5.查詢某活動的特定狀態報名紀錄
    List<Registration>findByActivityIdAndStatus(
            Long activityId,
            RegistrationStatus status
    );

    //====繳費查詢====//
    //6.查詢某會員的待繳費活動
    List<Registration>findByUserIdAndPaymentStatus(
            String userId,
            PaymentStatus paymentStatus
    );

    //====簽到記錄查詢====//
    //(管理員端only)
    //7.查詢某活動已簽到記錄
    List<Registration> findByActivityIdAndCheckedInTrue(Long activityId);

    //8.統計某活動的有效報名人數
    Long countByActivityId(Long activityId);
    //有效報名人數
    @Query("SELECT COUNT(r) FROM Registration r " +
            "WHERE r.activityId = :activityId " +
            "AND r.status != 'CANCELLED'")
    Long countValidRegistrations(@Param("activityId") Long activityId);
    //9.統計某活動的已簽到人數
    Long countByActivityIdAndCheckedInTrue(Long activityId);

    //====複雜查詢====//
    //10.查詢某會員在特定時間範圍內的報名紀錄
    @Query("SELECT r FROM Registration r " +
            "WHERE r.userId = :userId "+
            "AND r.registrationTime BETWEEN :startTime AND :endTime "+
            "ORDER BY r.registrationTime DESC")
    List<Registration>findByUserIdAndRegistrationTimeBetween(
            @Param("userId") String userId,
            @Param("startTime")LocalDateTime startTime,
            @Param("endTime")LocalDateTime endTime
            );
}
