package com.sms.model;

import java.time.Instant;

public class SmsEvent {
    private String phoneNumber;
    private String message;
    private String status;
    private String sentAt;

    public SmsEvent() {}

    public SmsEvent(String phoneNumber, String message, String status) {
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.status = status;
        this.sentAt = Instant.now().toString();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
