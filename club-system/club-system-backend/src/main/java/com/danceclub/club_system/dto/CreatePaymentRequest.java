package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Create Payment Request DTO
 */
public class CreatePaymentRequest {
    
    @NotNull(message = "報名 ID 不能為空")
    private Long registrationId;
    
    @NotNull(message = "繳費類型不能為空")
    private PaymentType paymentType;
    
    @NotNull(message = "金額不能為空")
    @Positive(message = "金額必須大於 0")
    private BigDecimal amount;
    
    @NotNull(message = "原價不能為空")
    @Positive(message = "原價必須大於 0")
    private BigDecimal originalAmount;
    
    private String discountReason;

    // Constructors
    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long registrationId, PaymentType paymentType, 
                               BigDecimal amount, BigDecimal originalAmount, 
                               String discountReason) {
        this.registrationId = registrationId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.originalAmount = originalAmount;
        this.discountReason = discountReason;
    }

    // Getters and Setters
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }
}
