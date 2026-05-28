package com.sms.service;

import com.sms.kafka.SmsEventProducer;
import com.sms.model.SmsEvent;
import com.sms.model.SmsRequest;
import com.sms.model.SmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private BlockListService blockListService;

    @Autowired
    private SmsVendorService vendorService;

    @Autowired
    private SmsEventProducer eventProducer;

    @Autowired
    private SmsStoreClient smsStoreClient;

    public SmsResponse send(SmsRequest request) {
        String phone = request.getPhoneNumber();
        String message = request.getMessage();

        if (phone == null || phone.isBlank()) {
            return new SmsResponse("ERROR", "Phone number is required");
        }
        if (message == null || message.isBlank()) {
            return new SmsResponse("ERROR", "Message is required");
        }

        // Redis check — fail-closed (block SMS if Redis is down for safety)
        try {
            if (blockListService.isBlocked(phone)) {
                log.warn("User {} is blocked", phone);
                return new SmsResponse("BLOCKED", "User is in the block list");
            }
        } catch (Exception e) {
            log.error("Redis is unreachable: {}. Blocking SMS for safety (fail-closed).", e.getMessage());
            return new SmsResponse("ERROR", "Service temporarily unavailable. Please try again later.");
        }

        // Check Go service connectivity
        if (!smsStoreClient.isGoServiceHealthy()) {
            log.warn("Go SMS Store service is down. SMS will still be sent but storage may be delayed.");
        }

        // 3P vendor call
        String vendorStatus;
        try {
            vendorStatus = vendorService.sendSms(phone, message);
        } catch (Exception e) {
            log.error("3P vendor call failed: {}", e.getMessage());
            return new SmsResponse("FAIL", "SMS vendor is unavailable");
        }

        // Kafka publish — don't silently lose the message
        try {
            SmsEvent event = new SmsEvent(request.getUserId(), phone, message, vendorStatus);
            eventProducer.publishSmsEvent(event);
        } catch (Exception e) {
            log.error("Kafka is down: {}. SMS was sent but event not logged.", e.getMessage());
            return new SmsResponse("PARTIAL", "SMS sent but failed to log event. Please retry.");
        }

        if ("FAIL".equals(vendorStatus)) {
            return new SmsResponse("FAIL", "SMS delivery failed");
        }

        return new SmsResponse("SUCCESS", "SMS sent successfully");
    }
}