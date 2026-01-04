package com.club.management.repository;

import com.club.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Find payments by status
     * @param status the payment status
     * @return list of payments with the given status
     */
    List<Payment> findByStatus(String status);
    
    /**
     * Find payments by payment method
     * @param method the payment method (cash or online)
     * @return list of payments with the given method
     */
    List<Payment> findByMethod(String method);
    
    /**
     * Find payments by status and method
     * @param status the payment status
     * @param method the payment method
     * @return list of payments matching the criteria
     */
    List<Payment> findByStatusAndMethod(String status, String method);
    
    /**
     * Find payment by registration ID
     * @param registrationId the registration ID
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByRegistrationId(String registrationId);
    
    /**
     * Find all payments by registration ID
     * @param registrationId the registration ID
     * @return list of payments for the registration
     */
    List<Payment> findAllByRegistrationId(String registrationId);
    
    /**
     * Find payments by status ordered by creation time
     * @param status the payment status
     * @return list of payments ordered by creation time
     */
    List<Payment> findByStatusOrderByCreatedAtAsc(String status);
    
    /**
     * Find payments reviewed by a specific admin
     * @param reviewedBy the admin user ID
     * @return list of payments reviewed by the admin
     */
    List<Payment> findByReviewedBy(String reviewedBy);
    
    /**
     * Find pending cash payments (status = pending, method = cash)
     * @return list of pending cash payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'pending' AND p.method = 'cash' ORDER BY p.createdAt ASC")
    List<Payment> findPendingCashPayments();
    
    /**
     * Find pending refund requests (status = refund_pending)
     * @return list of pending refund requests
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'refund_pending' ORDER BY p.createdAt ASC")
    List<Payment> findPendingRefunds();
    
    /**
     * Find payments by user (through registration)
     * @param userId the user ID
     * @return list of payments for the user
     */
    @Query("SELECT p FROM Payment p JOIN ActivityRegistration r ON p.registrationId = r.id WHERE r.userId = :userId")
    List<Payment> findByUserId(@Param("userId") String userId);
    
    /**
     * Find payments by user and status
     * @param userId the user ID
     * @param status the payment status
     * @return list of payments matching the criteria
     */
    @Query("SELECT p FROM Payment p JOIN ActivityRegistration r ON p.registrationId = r.id WHERE r.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
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
    long countByStatus(String status);
    
    /**
     * Calculate total amount by status
     * @param status the payment status
     * @return the total amount
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    Long sumAmountByStatus(@Param("status") String status);
    
    /**
     * Find overdue payments (pending for more than specified days)
     * @param daysAgo the date threshold
     * @return list of overdue payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'pending' AND p.createdAt < :daysAgo")
    List<Payment> findOverduePayments(@Param("daysAgo") LocalDateTime daysAgo);
    
    /**
     * Find the maximum payment ID for ID generation
     * @return the maximum payment ID or null if no payments exist
     */
    @Query("SELECT p.id FROM Payment p ORDER BY p.id DESC LIMIT 1")
    String findMaxId();
}
