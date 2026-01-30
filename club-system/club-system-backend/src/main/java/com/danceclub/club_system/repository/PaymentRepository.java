package com.danceclub.club_system.repository;

import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.enums.PaymentMethod;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payments by status
     * @param status the payment status
     * @return list of payments with the given status
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * Find payments by payment method
     * @param method the payment method
     * @return list of payments with the given method
     */
    List<Payment> findByMethod(PaymentMethod method);
    
    /**
     * Find payments by payment type
     * @param paymentType the payment type
     * @return list of payments with the given type
     */
    List<Payment> findByPaymentType(PaymentType paymentType);
    
    /**
     * Find payments by status and method
     * @param status the payment status
     * @param method the payment method
     * @return list of payments matching the criteria
     */
    List<Payment> findByStatusAndMethod(PaymentStatus status, PaymentMethod method);
    
    /**
     * Find payment by registration
     * @param registration the registration entity
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByRegistration(com.danceclub.club_system.model.Registration registration);
    
    /**
     * Find all payments by registration
     * @param registration the registration entity
     * @return list of payments for the registration
     */
    List<Payment> findAllByRegistration(com.danceclub.club_system.model.Registration registration);
    
    /**
     * Find payments by status ordered by creation time
     * @param status the payment status
     * @return list of payments ordered by creation time
     */
    List<Payment> findByStatusOrderByCreatedAtAsc(PaymentStatus status);
    
    /**
     * Find payments reviewed by a specific admin
     * @param reviewedBy the admin user ID
     * @return list of payments reviewed by the admin
     */
    List<Payment> findByReviewedBy(String reviewedBy);
    
    /**
     * Find pending cash payments
     * @return list of pending cash payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.method = 'CASH' ORDER BY p.createdAt ASC")
    List<Payment> findPendingCashPayments();
    
    /**
     * Find pending refund requests
     * @return list of pending refund requests
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'REFUNDED' OR p.status = 'PARTIAL_REFUNDED' ORDER BY p.createdAt ASC")
    List<Payment> findRefundedPayments();
    
    /**
     * Find payments by user (through registration)
     * @param userId the user ID
     * @return list of payments for the user
     */
    @Query("SELECT p FROM Payment p JOIN p.registration r WHERE r.userId = :userId")
    List<Payment> findByUserId(@Param("userId") String userId);
    
    /**
     * Find payments by user and status
     * @param userId the user ID
     * @param status the payment status
     * @return list of payments matching the criteria
     */
    @Query("SELECT p FROM Payment p JOIN p.registration r WHERE r.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") PaymentStatus status);
    
    /**
     * Find payments paid between two dates
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of payments paid in the given time range
     */
    List<Payment> findByPaidAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Count payments by status
     * @param status the payment status
     * @return the count of payments
     */
    long countByStatus(PaymentStatus status);
    
    /**
     * Calculate total amount by status
     * @param status the payment status
     * @return the total amount
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
    
    /**
     * Find overdue payments (pending for more than specified days)
     * @param daysAgo the date threshold
     * @return list of overdue payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :daysAgo")
    List<Payment> findOverduePayments(@Param("daysAgo") LocalDateTime daysAgo);
    
    /**
     * Find payments by bank account proof
     * @param bankAccountProof the last 5 digits of bank account
     * @return list of payments with the given bank account proof
     */
    List<Payment> findByBankAccountProof(Long bankAccountProof);
}
