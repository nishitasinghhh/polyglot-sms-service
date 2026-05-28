package com.sms;

import com.sms.kafka.SmsEventProducer;
import com.sms.model.SmsEvent;
import com.sms.model.SmsRequest;
import com.sms.model.SmsResponse;
import com.sms.service.BlockListService;
import com.sms.service.SmsService;
import com.sms.service.SmsVendorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class SmsServiceTest {

    @Autowired
    private SmsService smsService;

    @MockitoBean
    private BlockListService blockListService;

    @MockitoBean
    private SmsVendorService vendorService;

    @MockitoBean
    private SmsEventProducer eventProducer;

    @Test
    void shouldRejectBlockedUser() {
        when(blockListService.isBlocked("+91blocked")).thenReturn(true);

        SmsResponse response = smsService.send(new SmsRequest("user1", "+91blocked", "hello"));

        assertEquals("BLOCKED", response.getStatus());
        verify(vendorService, never()).sendSms(any(), any());
        verify(eventProducer, never()).publishSmsEvent(any());
    }

    @Test
    void shouldSendSmsForNonBlockedUser() {
        when(blockListService.isBlocked("+91999")).thenReturn(false);
        when(vendorService.sendSms("+91999", "hello")).thenReturn("SUCCESS");

        SmsResponse response = smsService.send(new SmsRequest("user2", "+91999", "hello"));

        assertEquals("SUCCESS", response.getStatus());
        verify(eventProducer).publishSmsEvent(any(SmsEvent.class));
    }

    @Test
    void shouldReturnFailWhenVendorFails() {
        when(blockListService.isBlocked("+91888")).thenReturn(false);
        when(vendorService.sendSms("+91888", "hello")).thenReturn("FAIL");

        SmsResponse response = smsService.send(new SmsRequest("user3", "+91888", "hello"));

        assertEquals("FAIL", response.getStatus());
        verify(eventProducer).publishSmsEvent(any(SmsEvent.class));
    }

    @Test
    void shouldRejectEmptyPhoneNumber() {
        SmsResponse response = smsService.send(new SmsRequest("user4", "", "hello"));
        assertEquals("ERROR", response.getStatus());
    }

    @Test
    void shouldRejectEmptyMessage() {
        SmsResponse response = smsService.send(new SmsRequest("user5", "+91999", ""));
        assertEquals("ERROR", response.getStatus());
    }
}