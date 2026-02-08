package com.danceclub.club_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 綠界 ECPay 配置
 */
@Configuration
public class EcpayConfig {

    @Value("${ecpay.merchant-id}")
    private String merchantId;

    @Value("${ecpay.hash-key}")
    private String hashKey;

    @Value("${ecpay.hash-iv}")
    private String hashIv;

    @Value("${ecpay.api-url}")
    private String apiUrl;

    @Value("${ecpay.query-url}")
    private String queryUrl;

    @Value("${ecpay.return-url}")
    private String returnUrl;

    @Value("${ecpay.notify-url}")
    private String notifyUrl;

    public String getMerchantId() {
        return merchantId;
    }

    public String getHashKey() {
        return hashKey;
    }

    public String getHashIv() {
        return hashIv;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getQueryUrl() {
        return queryUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }
}
