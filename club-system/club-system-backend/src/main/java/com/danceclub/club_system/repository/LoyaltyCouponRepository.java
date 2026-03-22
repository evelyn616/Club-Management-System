package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.LoyaltyCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyCouponRepository extends JpaRepository<LoyaltyCoupon, Long> {

    /** 查詢某會員所有優惠券 */
    List<LoyaltyCoupon> findByUserIdOrderByEarnedAtDesc(String userId);

    /** 查詢某會員的未使用優惠券 */
    List<LoyaltyCoupon> findByUserIdAndIsUsedFalseOrderByEarnedAtAsc(String userId);

    /** 計算某會員共發放過幾張券（含已使用） */
    long countByUserId(String userId);

    /** 計算某會員未使用優惠券數量 */
    long countByUserIdAndIsUsedFalse(String userId);

    /** 查詢所有優惠券（管理員用） */
    List<LoyaltyCoupon> findAllByOrderByEarnedAtDesc();
}
