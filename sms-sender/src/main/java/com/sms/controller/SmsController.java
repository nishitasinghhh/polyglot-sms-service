package com.sms.controller;

import com.sms.model.SmsRequest;
import com.sms.model.SmsResponse;
import com.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/v1/sms/send")
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        SmsResponse response = smsService.send(request);
        return ResponseEntity.ok(response);
    }
}