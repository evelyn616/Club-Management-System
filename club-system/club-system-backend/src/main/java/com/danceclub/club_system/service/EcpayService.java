package com.danceclub.club_system.service;

import com.danceclub.club_system.config.EcpayConfig;
import com.danceclub.club_system.dto.EcpayCheckoutRequest;
import com.danceclub.club_system.dto.EcpayCheckoutResponse;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 綠界 ECPay 金流服務
 */
@Service
public class EcpayService {

    private final EcpayConfig ecpayConfig;
    private final PaymentRepository paymentRepository;

    public EcpayService(EcpayConfig ecpayConfig, PaymentRepository paymentRepository) {
        this.ecpayConfig = ecpayConfig;
        this.paymentRepository = paymentRepository;
    }

    /**
     * 創建綠界結帳訂單
     */
    @Transactional
    public EcpayCheckoutResponse createCheckout(EcpayCheckoutRequest request) {
        // 查詢 Payment
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // 生成訂單編號（使用 Payment ID + 時間戳）
        String merchantTradeNo = generateMerchantTradeNo(payment.getId());

        // 準備綠界參數
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", ecpayConfig.getMerchantId());
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", getCurrentDateTime());
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(request.getTotalAmount().intValue()));
        params.put("TradeDesc", request.getTradeDesc());
        params.put("ItemName", request.getItemName());
        params.put("ReturnURL", ecpayConfig.getReturnUrl());
        params.put("ChoosePayment", request.getChoosePayment());
        params.put("EncryptType", "1");

        // 計算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);

        // 更新 Payment 狀態和訂單編號
        payment.setMerchantTradeNo(merchantTradeNo);
        payment.setMethod(mapChoosePaymentToMethod(request.getChoosePayment()));
        
        // 設定付款期限（預設 3 天）
        payment.setPaymentDeadline(LocalDateTime.now().plusDays(3));
        
        paymentRepository.save(payment);

        // 返回表單參數（前端需要用 POST 提交）
        return new EcpayCheckoutResponse(ecpayConfig.getApiUrl(), merchantTradeNo, checkMacValue, params);
    }

    /**
     * 處理綠界付款通知（Notify）
     */
    @Transactional
    public String handlePaymentNotify(Map<String, String> params) {
        // 驗證 CheckMacValue
        String receivedCheckMacValue = params.get("CheckMacValue");
        params.remove("CheckMacValue");
        
        String calculatedCheckMacValue = generateCheckMacValue(params);
        
        if (!calculatedCheckMacValue.equalsIgnoreCase(receivedCheckMacValue)) {
            throw new IllegalArgumentException("CheckMacValue verification failed");
        }

        // 取得訂單編號和付款狀態
        String merchantTradeNo = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode");
        String tradeNo = params.get("TradeNo"); // 綠界交易編號
        String paymentType = params.get("PaymentType"); // 付款方式

        // 從 MerchantTradeNo 解析 Payment ID
        Long paymentId = extractPaymentIdFromTradeNo(merchantTradeNo);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // 儲存綠界交易編號
        payment.setEcpayTradeNo(tradeNo);
        
        // 儲存完整回應（JSON 格式）
        payment.setEcpayResponse(params.toString());

        // 更新付款狀態
        if ("1".equals(rtnCode)) {
            // 付款成功
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            
            // 處理 ATM 或超商付款的額外資訊
            if ("ATM".equals(paymentType)) {
                payment.setAtmBankCode(params.get("BankCode"));
                payment.setAtmAccount(params.get("vAccount"));
            } else if ("CVS".equals(paymentType)) {
                payment.setCvsPaymentCode(params.get("PaymentNo"));
                payment.setCvsType(params.get("PaymentType"));
            }
            
            payment.setNote("付款成功 - ECPay TradeNo: " + tradeNo);
        } else {
            // 付款失敗
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("RtnCode: " + rtnCode + ", RtnMsg: " + params.get("RtnMsg"));
            payment.setNote("付款失敗 - MerchantTradeNo: " + merchantTradeNo);
        }

        paymentRepository.save(payment);

        // 回傳 1|OK 給綠界
        return "1|OK";
    }

    /**
     * 將綠界付款方式對應到系統的 PaymentMethod
     */
    private com.danceclub.club_system.model.enums.PaymentMethod mapChoosePaymentToMethod(String choosePayment) {
        return switch (choosePayment) {
            case "Credit" -> com.danceclub.club_system.model.enums.PaymentMethod.CREDIT_CARD;
            case "ATM" -> com.danceclub.club_system.model.enums.PaymentMethod.BANK_TRANSFER;
            case "CVS" -> com.danceclub.club_system.model.enums.PaymentMethod.OTHER;
            case "BARCODE" -> com.danceclub.club_system.model.enums.PaymentMethod.OTHER;
            default -> com.danceclub.club_system.model.enums.PaymentMethod.OTHER;
        };
    }

    /**
     * 生成訂單編號
     * 格式: P{paymentId}_{timestamp}
     */
    private String generateMerchantTradeNo(Long paymentId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "P" + paymentId + "_" + timestamp;
    }

    /**
     * 從訂單編號解析 Payment ID
     */
    private Long extractPaymentIdFromTradeNo(String merchantTradeNo) {
        // 格式: P{paymentId}_{timestamp}
        String[] parts = merchantTradeNo.split("_");
        if (parts.length > 0 && parts[0].startsWith("P")) {
            return Long.parseLong(parts[0].substring(1));
        }
        throw new IllegalArgumentException("Invalid MerchantTradeNo format");
    }

    /**
     * 取得當前時間（綠界格式）
     */
    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    /**
     * 生成 CheckMacValue
     */
    private String generateCheckMacValue(Map<String, String> params) {
        // 排序參數（TreeMap 已自動排序）
        Map<String, String> sortedParams = new TreeMap<>(params);

        // 組合參數字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(ecpayConfig.getHashKey());
        
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        sb.append("&HashIV=").append(ecpayConfig.getHashIv());

        // URL Encode
        String encodedString = urlEncode(sb.toString());

        // 轉小寫
        encodedString = encodedString.toLowerCase();

        // SHA256 加密
        return sha256(encodedString).toUpperCase();
    }

    /**
     * URL Encode
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL encoding failed", e);
        }
    }

    /**
     * SHA256 加密
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * 建立結帳 URL（帶參數）
     */
    private String buildCheckoutUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder(ecpayConfig.getApiUrl());
        url.append("?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                url.append("&");
            }
            try {
                url.append(entry.getKey())
                   .append("=")
                   .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("URL encoding failed", e);
            }
            first = false;
        }
        
        return url.toString();
    }
}
