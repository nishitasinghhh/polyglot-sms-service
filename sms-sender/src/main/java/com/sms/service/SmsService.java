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

    public SmsResponse send(SmsRequest request) {
        String phone = request.getPhoneNumber();
        String message = request.getMessage();

        if (phone == null || phone.isBlank()) {
            return new SmsResponse("ERROR", "Phone number is required");
        }
        if (message == null || message.isBlank()) {
            return new SmsResponse("ERROR", "Message is required");
        }

        if (blockListService.isBlocked(phone)) {
            log.warn("User {} is blocked", phone);
            return new SmsResponse("BLOCKED", "User is in the block list");
        }

        String vendorStatus = vendorService.sendSms(phone, message);

        SmsEvent event = new SmsEvent(phone, message, vendorStatus);
        eventProducer.publishSmsEvent(event);

        if ("FAIL".equals(vendorStatus)) {
            return new SmsResponse("FAIL", "SMS delivery failed");
        }

        return new SmsResponse("SUCCESS", "SMS sent successfully");
    }
}
