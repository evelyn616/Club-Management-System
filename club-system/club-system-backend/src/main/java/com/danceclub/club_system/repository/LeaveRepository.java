package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// --- 修正 1：務必取消註解，否則會報 Cannot find symbol ---
import com.example.demo.entity.MemberEntity;
import com.example.demo.entity.ActivityEntity;
import com.example.demo.entity.LeaveEntity;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveEntity, Long> {
 
    /**
     * 1. 根據員工查詢
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.member = :member")
    List<LeaveEntity> findLeaveRequestsByMemberQuery(MemberEntity member);

    /**
     * 2. 根據狀態查詢
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.status = :status")
    List<LeaveEntity> findLeaveRequestsByStatusQuery(String status);

    /**
     * 3. 根據審核人查詢
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.reviewedBy = :reviewedBy")
    List<LeaveEntity> findLeaveRequestsByReviewedByQuery(MemberEntity reviewedBy);

    /**
     * 4. 根據活動查詢
     * --- 修正 2：JPQL 應指向 Entity 裡的屬性名稱 ---
     * 如果你在 Entity 裡寫的是 private ActivityEntity activity; 
     * 那麼這裡就要寫 l.activity = :activity
     */
    @Query("SELECT l FROM LeaveEntity l WHERE l.activity = :activity")
    List<LeaveEntity> findLeaveRequestsByActivityQuery(ActivityEntity activity);

    /**
     * 5. 根據最近時間查詢 (這是 Derived Query，寫法正確)
     */
    List<LeaveEntity> findByCreatedAtAfter(LocalDateTime dateTime);
}