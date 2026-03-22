package com.danceclub.club_system.service;

import com.danceclub.club_system.config.EcpayConfig;
import com.danceclub.club_system.dto.EcpayCheckoutRequest;
import com.danceclub.club_system.dto.EcpayCheckoutResponse;
import com.danceclub.club_system.model.Payment;
import com.danceclub.club_system.model.Registration;
import com.danceclub.club_system.model.enums.PaymentStatus;
import com.danceclub.club_system.repository.PaymentRepository;
import com.danceclub.club_system.repository.RegistrationRepository;
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
    private final RegistrationRepository registrationRepository;

    public EcpayService(EcpayConfig ecpayConfig, PaymentRepository paymentRepository, RegistrationRepository registrationRepository) {
        this.ecpayConfig = ecpayConfig;
        this.paymentRepository = paymentRepository;
        this.registrationRepository = registrationRepository;
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
        payment.setStatus(PaymentStatus.PROCESSING);
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

        Registration registration = payment.getRegistration();
        // 儲存綠界交易編號
        payment.setEcpayTradeNo(tradeNo);
        
        // 儲存完整回應（JSON 格式）
        payment.setEcpayResponse(params.toString());

        // 更新付款狀態
        if ("1".equals(rtnCode)) {
            // 付款成功
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            registration.setPaymentStatus(PaymentStatus.PAID);

            
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
            registration.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("RtnCode: " + rtnCode + ", RtnMsg: " + params.get("RtnMsg"));
            payment.setNote("付款失敗 - MerchantTradeNo: " + merchantTradeNo);
        }

        paymentRepository.save(payment);
        registrationRepository.save(registration);

        // 回傳 1|OK 給綠界
        return "1|OK";
    }

    /**
     * 查詢綠界訂單狀態（QueryTradeInfo）
     */
    public Map<String, String> queryTradeInfo(String merchantTradeNo) {
        // 準備查詢參數
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", ecpayConfig.getMerchantId());
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("TimeStamp", String.valueOf(System.currentTimeMillis() / 1000));

        // 計算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 發送 POST 請求到綠界
        try {
            StringBuilder postData = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) postData.append("&");
                postData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                first = false;
            }

            java.net.URL url = new java.net.URL(ecpayConfig.getQueryUrl());
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(postData.toString().getBytes(StandardCharsets.UTF_8));
            }

            // 讀取回應
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // 解析回應（格式：key=value&key=value）
            Map<String, String> result = new LinkedHashMap<>();
            String[] pairs = response.toString().split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = java.net.URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.toString());
                    String value = idx < pair.length() - 1
                            ? java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.toString())
                            : "";
                    result.put(key, value);
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("查詢綠界訂單失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 查詢並同步綠界訂單狀態到本地資料庫
     */
    @Transactional
    public Map<String, Object> queryAndSyncTradeStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("繳費記錄不存在"));

        if (payment.getMerchantTradeNo() == null || payment.getMerchantTradeNo().isEmpty()) {
            throw new IllegalArgumentException("此筆繳費沒有綠界訂單編號");
        }

        // 查詢綠界
        Map<String, String> tradeInfo = queryTradeInfo(payment.getMerchantTradeNo());

        String tradeStatus = tradeInfo.get("TradeStatus");
        String tradeNo = tradeInfo.get("TradeNo");
        String tradeAmt = tradeInfo.get("TradeAmt");
        String paymentDateStr = tradeInfo.get("PaymentDate");
        String paymentTypeStr = tradeInfo.get("PaymentType");

        // 判斷是否需要同步狀態
        boolean synced = false;
        if ("1".equals(tradeStatus) && payment.getStatus() != PaymentStatus.PAID) {
            // 綠界已付款但本地還沒更新
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setEcpayTradeNo(tradeNo);
            payment.setNote("綠界查詢同步 - 付款成功, 付款方式: " + paymentTypeStr + ", 付款時間: " + paymentDateStr);

            Registration registration = payment.getRegistration();
            if (registration != null) {
                registration.setPaymentStatus(PaymentStatus.PAID);
                registrationRepository.save(registration);
            }

            paymentRepository.save(payment);
            synced = true;
        }

        // 回傳查詢結果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("paymentId", paymentId);
        result.put("merchantTradeNo", payment.getMerchantTradeNo());
        result.put("localStatus", payment.getStatus().name());
        result.put("ecpayTradeStatus", tradeStatus);
        result.put("ecpayTradeStatusText", "1".equals(tradeStatus) ? "已付款" : "0".equals(tradeStatus) ? "未付款" : "異常");
        result.put("ecpayTradeNo", tradeNo);
        result.put("tradeAmt", tradeAmt);
        result.put("paymentDate", paymentDateStr);
        result.put("paymentType", paymentTypeStr);
        result.put("synced", synced);
        result.put("rawResponse", tradeInfo);

        return result;
    }

    /**
     * 將綠界付款方式對應到系統的 PaymentMethod
     */
    private com.danceclub.club_system.model.enums.PaymentMethod mapChoosePaymentToMethod(String choosePayment) {
        return switch (choosePayment) {
            case "Credit" -> com.danceclub.club_system.model.enums.PaymentMethod.CREDIT_CARD;
            case "ATM" -> com.danceclub.club_system.model.enums.PaymentMethod.BANK_TRANSFER;
            case "CVS" -> com.danceclub.club_system.model.enums.PaymentMethod.CVS;
            case "BARCODE" -> com.danceclub.club_system.model.enums.PaymentMethod.BARCODE;
            default -> com.danceclub.club_system.model.enums.PaymentMethod.OTHER;
        };
    }

    /**
     * 生成訂單編號
     * 格式: P{paymentId}{timestamp} (移除底線，綠界只接受數字和英文字母)
     */
    private String generateMerchantTradeNo(Long paymentId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "P" + paymentId + timestamp;
    }

    /**
     * 從訂單編號解析 Payment ID
     */
    private Long extractPaymentIdFromTradeNo(String merchantTradeNo) {
        // 格式: P{paymentId}{timestamp}
        // 移除 P 前綴，然後從右邊移除 14 位時間戳
        if (merchantTradeNo.startsWith("P") && merchantTradeNo.length() > 15) {
            String withoutPrefix = merchantTradeNo.substring(1);
            String paymentIdStr = withoutPrefix.substring(0, withoutPrefix.length() - 14);
            return Long.parseLong(paymentIdStr);
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
     * 生成 CheckMacValue（按照綠界官方規範）
     */
    private String generateCheckMacValue(Map<String, String> params) {
        // 1) 移除 CheckMacValue（如果存在）
        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("CheckMacValue");

        // 2) 按鍵值 A-Z 排序（TreeMap 已自動排序）
        
        // 3) 將陣列轉為 query 字串
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!first) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        // 4) 最前方加入 HashKey，最後方加入 HashIV
        String paramsString = "HashKey=" + ecpayConfig.getHashKey() + "&" + queryString.toString() + "&HashIV=" + ecpayConfig.getHashIv();

        // 5) 做 URLEncode
        String encodedString;
        try {
            encodedString = URLEncoder.encode(paramsString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL encoding failed", e);
        }

        // 6) 轉為全小寫
        encodedString = encodedString.toLowerCase();

        // 7) 轉換特定字元（與 .NET 相符的字元）
        encodedString = encodedString
                .replace("%2d", "-")
                .replace("%5f", "_")
                .replace("%2e", ".")
                .replace("%21", "!")
                .replace("%2a", "*")
                .replace("%28", "(")
                .replace("%29", ")");

        // 8) 進行 SHA256 編碼
        String hashedString = sha256(encodedString);

        // 9) 轉為全大寫後回傳
        return hashedString.toUpperCase();
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
