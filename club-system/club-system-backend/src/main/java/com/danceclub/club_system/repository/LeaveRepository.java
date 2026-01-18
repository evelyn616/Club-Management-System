package com.danceclub.club_system.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.danceclub.club_system.model.User;
import com.danceclub.club_system.model.Activity;
import com.danceclub.club_system.model.LeaveEntity;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveEntity, Long> {

    /**
     * 1. 根據社員查詢 
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.member = :member")
    List<LeaveEntity> findLeaveRequestsByMemberQuery(@Param("member") User member);

    /**
     * 2. 根據狀態查詢
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.status = :status")
    List<LeaveEntity> findLeaveRequestsByStatusQuery(@Param("status") String status);

    /**
     * 3. 根據審核人查詢 
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.reviewedBy = :reviewedBy")
    List<LeaveEntity> findLeaveRequestsByReviewedByQuery(@Param("reviewedBy") User reviewedBy);

    /**
     * 4. 根據活動查詢
     * JPQL 必須對應 LeaveEntity 裡面的屬性名稱 l.activity
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.activity = :activity")
    List<LeaveEntity> findLeaveRequestsByActivityQuery(@Param("activity") Activity activity);

    /**
     * 5. 根據最近時間查詢 (這是 Derived Query)
     */
    List<LeaveEntity> findByCreatedAtAfter(LocalDateTime dateTime);
    

    List<LeaveEntity> findByMember_Id(String userId);
}