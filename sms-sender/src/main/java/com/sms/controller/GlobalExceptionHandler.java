package com.sms.controller;

import com.sms.model.SmsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SmsResponse> handleBadJson(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new SmsResponse("ERROR", "Invalid JSON format. Please check your request body."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SmsResponse> handleGenericError(Exception e) {
        return ResponseEntity.internalServerError()
                .body(new SmsResponse("ERROR", "An unexpected error occurred."));
    }
}
