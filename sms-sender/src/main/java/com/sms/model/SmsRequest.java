package com.sms.model;

public class SmsRequest {
    private String userId;
    private String phoneNumber;
    private String message;

    public SmsRequest() {}

    public SmsRequest(String userId, String phoneNumber, String message) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}