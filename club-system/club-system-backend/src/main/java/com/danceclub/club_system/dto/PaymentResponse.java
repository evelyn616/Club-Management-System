package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.PaymentMethod;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 */
public class PaymentResponse {
    private Long id;
    private Long registrationId;
    private PaymentType paymentType;
    private BigDecimal originalAmount;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private String discountReason;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDateTime paidAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNote;
    private String note;
    private Long bankAccountProof;
    private String merchantTradeNo;
    private String ecpayTradeNo;
    private LocalDateTime paymentDeadline;
    private String atmAccount;
    private String atmBankCode;
    private String cvsPaymentCode;
    private String cvsType;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String userEmail;
    private String userId;

    // Constructors
    public PaymentResponse() {}

    public PaymentResponse(Long id, Long registrationId, PaymentType paymentType,
                          BigDecimal originalAmount, BigDecimal amount, BigDecimal discountAmount,
                          String discountReason, PaymentStatus status, PaymentMethod method,
                          LocalDateTime paidAt, String reviewedBy, LocalDateTime reviewedAt,
                          String reviewNote, String note, Long bankAccountProof,
                          String merchantTradeNo, String ecpayTradeNo, LocalDateTime paymentDeadline,
                          String atmAccount, String atmBankCode, String cvsPaymentCode,
                          String cvsType, String failureReason, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.registrationId = registrationId;
        this.paymentType = paymentType;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.discountAmount = discountAmount;
        this.discountReason = discountReason;
        this.status = status;
        this.method = method;
        this.paidAt = paidAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNote = reviewNote;
        this.note = note;
        this.bankAccountProof = bankAccountProof;
        this.merchantTradeNo = merchantTradeNo;
        this.ecpayTradeNo = ecpayTradeNo;
        this.paymentDeadline = paymentDeadline;
        this.atmAccount = atmAccount;
        this.atmBankCode = atmBankCode;
        this.cvsPaymentCode = cvsPaymentCode;
        this.cvsType = cvsType;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getBankAccountProof() {
        return bankAccountProof;
    }

    public void setBankAccountProof(Long bankAccountProof) {
        this.bankAccountProof = bankAccountProof;
    }

    public String getMerchantTradeNo() {
        return merchantTradeNo;
    }

    public void setMerchantTradeNo(String merchantTradeNo) {
        this.merchantTradeNo = merchantTradeNo;
    }

    public String getEcpayTradeNo() {
        return ecpayTradeNo;
    }

    public void setEcpayTradeNo(String ecpayTradeNo) {
        this.ecpayTradeNo = ecpayTradeNo;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public String getAtmAccount() {
        return atmAccount;
    }

    public void setAtmAccount(String atmAccount) {
        this.atmAccount = atmAccount;
    }

    public String getAtmBankCode() {
        return atmBankCode;
    }

    public void setAtmBankCode(String atmBankCode) {
        this.atmBankCode = atmBankCode;
    }

    public String getCvsPaymentCode() {
        return cvsPaymentCode;
    }

    public void setCvsPaymentCode(String cvsPaymentCode) {
        this.cvsPaymentCode = cvsPaymentCode;
    }

    public String getCvsType() {
        return cvsType;
    }

    public void setCvsType(String cvsType) {
        this.cvsType = cvsType;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
