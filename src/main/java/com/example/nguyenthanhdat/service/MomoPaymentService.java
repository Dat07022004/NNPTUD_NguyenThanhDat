package com.example.nguyenthanhdat.service;

import com.example.nguyenthanhdat.config.MomoProperties;
import com.example.nguyenthanhdat.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MomoPaymentService {
    private static final Logger log = LoggerFactory.getLogger(MomoPaymentService.class);

    private final MomoProperties momoProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public MomoPaymentService(MomoProperties momoProperties) {
        this.momoProperties = momoProperties;
    }

    public String createPaymentUrl(Order order) {
        String requestId = UUID.randomUUID().toString();
        String orderId = "ORD" + order.getId() + "T" + System.currentTimeMillis();
        String amount = String.valueOf(order.getTotalAmount().longValue());
        String orderInfo = "Thanh toan don hang " + order.getId();
        String extraData = "";
        String redirectUrl = momoProperties.getReturnUrl();
        String ipnUrl = momoProperties.getNotifyUrl();
        String requestType = normalizeRequestType(momoProperties.getRequestType());
        boolean autoCapture = true;

        String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoProperties.getPartnerCode()
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = hmacSHA256(rawSignature, momoProperties.getSecretKey());

        Map<String, Object> payload = new HashMap<>();
        payload.put("partnerCode", momoProperties.getPartnerCode());
        payload.put("partnerName", "Test");
        payload.put("storeId", "MomoTestStore");
        payload.put("requestId", requestId);
        payload.put("amount", amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipnUrl);
        payload.put("extraData", extraData);
        payload.put("requestType", requestType);
        payload.put("autoCapture", autoCapture);
        payload.put("lang", "vi");
        payload.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        Map<String, Object> response = restTemplate.postForObject(
                momoProperties.getMomoApiUrl(),
                request,
                Map.class
        );

        if (response == null) {
            throw new RuntimeException("MoMo response is empty");
        }

        Object resultCode = response.get("resultCode");
        if (resultCode == null || !"0".equals(String.valueOf(resultCode))) {
            String code = String.valueOf(response.getOrDefault("resultCode", "unknown"));
            String message = String.valueOf(response.getOrDefault("message", "Unknown MoMo error"));
            log.error("MoMo create payment failed. code={}, message={}, response={}", code, message, response);
            throw new RuntimeException("MoMo loi (code " + code + "): " + message);
        }

        Object payUrl = response.get("payUrl");
        if (payUrl == null || payUrl.toString().isBlank()) {
            throw new RuntimeException("MoMo khong tra ve payUrl");
        }

        return payUrl.toString();
    }

    private String normalizeRequestType(String configuredType) {
        if (configuredType == null || configuredType.isBlank()) {
            return "captureWallet";
        }
        if ("captureMoMoWallet".equalsIgnoreCase(configuredType)) {
            return "captureWallet";
        }
        return configuredType;
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] hashBytes = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Khong the tao chu ky MoMo", e);
        }
    }
}
