package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.enums.ActivityStatus;
import com.danceclub.club_system.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // TODO 1: 查詢所有已發布的活動，按開始時間升序排列
    List<Activity>findByStatusOrderByStartTimeAsc(ActivityStatus status);


    // TODO 2: 根據活動類型和狀態查詢，按開始時間升序排列
    List<Activity>findByActivityTypeAndStatusOrderByStartTimeAsc(
            ActivityType activityType,
            ActivityStatus status);

    // TODO 3: 查詢特定狀態的活動，按開始時間降序排列（最新的在前面）
    List<Activity>findByStatusOrderByStartTimeDesc(ActivityStatus status);

    // TODO 4: 關鍵字搜尋（標題或描述）
    @Query("SELECT a FROM Activity a WHERE (a.title LIKE CONCAT('%', :keyword, '%') OR a.description LIKE CONCAT('%', :keyword, '%')) AND a.status = :status ORDER BY a.startTime ASC")
    List<Activity> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") ActivityStatus status
    );

    // TODO 5: 查詢可報名活動
    @Query("SELECT a FROM Activity a " +
            "WHERE a.status = :status " +
            "AND (a.registrationDeadline IS NULL OR a.registrationDeadline > :now) " +
            "AND a.startTime > :now " +
            "ORDER BY a.startTime ASC")
    List<Activity> findRegistrableActivities(
            @Param("status") ActivityStatus status,
            @Param("now") LocalDateTime now
    );
    // TODO 6: 查看近七天的活動
    @Query("SELECT a FROM Activity a " +
            "WHERE a.startTime BETWEEN :now AND :sevenDaysLater " +
            "AND a.status = :status " +
            "ORDER BY a.startTime ASC")
    List<Activity> findUpcomingActivities(
            @Param("status") ActivityStatus status,
            @Param("now") LocalDateTime now,
            @Param("sevenDaysLater") LocalDateTime sevenDaysLater
    );

    // 查詢某個使用者建立的所有活動，按建立時間降序排列
    List<Activity> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
