package com.sms.controller;

import com.sms.model.SmsRequest;
import com.sms.model.SmsResponse;
import com.sms.service.BlockListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsController {

    @Autowired
    private BlockListService blockListService;

    @PostMapping("/v1/sms/send")
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        if (blockListService.isBlocked(request.getPhoneNumber())) {
            return ResponseEntity.ok(new SmsResponse("BLOCKED", "User is blocked"));
        }
        return ResponseEntity.ok(new SmsResponse("SUCCESS", "SMS sent"));
    }
}