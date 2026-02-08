package com.danceclub.club_system.dto;

import java.math.BigDecimal;

/**
 * 綠界結帳請求 DTO
 */
public class EcpayCheckoutRequest {
    
    private Long paymentId;
    private String itemName;
    private BigDecimal totalAmount;
    private String tradeDesc;
    private String choosePayment; // Credit, ATM, CVS, BARCODE, ALL
    
    public EcpayCheckoutRequest() {}
    
    public EcpayCheckoutRequest(Long paymentId, String itemName, BigDecimal totalAmount, 
                                String tradeDesc, String choosePayment) {
        this.paymentId = paymentId;
        this.itemName = itemName;
        this.totalAmount = totalAmount;
        this.tradeDesc = tradeDesc;
        this.choosePayment = choosePayment;
    }
    
    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getTradeDesc() {
        return tradeDesc;
    }
    
    public void setTradeDesc(String tradeDesc) {
        this.tradeDesc = tradeDesc;
    }
    
    public String getChoosePayment() {
        return choosePayment;
    }
    
    public void setChoosePayment(String choosePayment) {
        this.choosePayment = choosePayment;
    }
}
