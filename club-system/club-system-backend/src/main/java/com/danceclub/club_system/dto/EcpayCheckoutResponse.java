package com.danceclub.club_system.dto;

import java.util.Map;

/**
 * 綠界結帳回應 DTO
 */
public class EcpayCheckoutResponse {
    
    private String actionUrl;
    private String merchantTradeNo;
    private String checkMacValue;
    private Map<String, String> formData;
    
    public EcpayCheckoutResponse() {}
    
    public EcpayCheckoutResponse(String actionUrl, String merchantTradeNo, String checkMacValue, Map<String, String> formData) {
        this.actionUrl = actionUrl;
        this.merchantTradeNo = merchantTradeNo;
        this.checkMacValue = checkMacValue;
        this.formData = formData;
    }
    
    // Getters and Setters
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public String getMerchantTradeNo() {
        return merchantTradeNo;
    }
    
    public void setMerchantTradeNo(String merchantTradeNo) {
        this.merchantTradeNo = merchantTradeNo;
    }
    
    public String getCheckMacValue() {
        return checkMacValue;
    }
    
    public void setCheckMacValue(String checkMacValue) {
        this.checkMacValue = checkMacValue;
    }
    
    public Map<String, String> getFormData() {
        return formData;
    }
    
    public void setFormData(Map<String, String> formData) {
        this.formData = formData;
    }
}
