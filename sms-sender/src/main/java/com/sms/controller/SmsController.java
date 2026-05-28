package com.sms.controller;

import com.sms.model.SmsRequest;
import com.sms.model.SmsResponse;
import com.sms.service.BlockListService;
import com.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private BlockListService blockListService;

    @PostMapping("/v1/sms/send")
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        SmsResponse response = smsService.send(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/sms/block/{phoneNumber}")
    public ResponseEntity<SmsResponse> blockUser(@PathVariable String phoneNumber) {
        blockListService.blockUser(phoneNumber);
        return ResponseEntity.ok(new SmsResponse("SUCCESS", phoneNumber + " has been blocked"));
    }

    @DeleteMapping("/v1/sms/block/{phoneNumber}")
    public ResponseEntity<SmsResponse> unblockUser(@PathVariable String phoneNumber) {
        blockListService.unblockUser(phoneNumber);
        return ResponseEntity.ok(new SmsResponse("SUCCESS", phoneNumber + " has been unblocked"));
    }
}