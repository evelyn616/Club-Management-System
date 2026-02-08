package com.danceclub.club_system.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Update Bank Account Proof Request DTO
 */
public class UpdateBankProofRequest {
    
    @NotNull(message = "轉帳後五碼不能為空")
    private Long bankAccountProof;

    // Constructors
    public UpdateBankProofRequest() {}

    public UpdateBankProofRequest(Long bankAccountProof) {
        this.bankAccountProof = bankAccountProof;
    }

    // Getters and Setters
    public Long getBankAccountProof() {
        return bankAccountProof;
    }

    public void setBankAccountProof(Long bankAccountProof) {
        this.bankAccountProof = bankAccountProof;
    }
}
