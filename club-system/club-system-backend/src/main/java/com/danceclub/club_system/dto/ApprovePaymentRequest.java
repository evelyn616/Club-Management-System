package com.danceclub.club_system.dto;

import com.danceclub.club_system.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Approve Payment Request DTO
 */
public class ApprovePaymentRequest {
    
    @NotNull(message = "付款方式不能為空")
    private PaymentMethod method;
    
    private String reviewNote;

    // Constructors
    public ApprovePaymentRequest() {}

    public ApprovePaymentRequest(PaymentMethod method, String reviewNote) {
        this.method = method;
        this.reviewNote = reviewNote;
    }

    // Getters and Setters
    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
