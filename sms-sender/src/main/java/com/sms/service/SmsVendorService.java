package com.sms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsVendorService {

    private static final Logger log = LoggerFactory.getLogger(SmsVendorService.class);

    public String sendSms(String phoneNumber, String message) {
        log.info("Calling 3P vendor for {} ...", phoneNumber);
        boolean success = Math.random() > 0.2;
        String status = success ? "SUCCESS" : "FAIL";
        log.info("3P vendor returned: {}", status);
        return status;
    }
}
