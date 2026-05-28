package com.sms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsStoreClient {

    private static final Logger log = LoggerFactory.getLogger(SmsStoreClient.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GO_SERVICE_URL = "http://localhost:8081";

    public boolean isGoServiceHealthy() {
        try {
            restTemplate.getForObject(GO_SERVICE_URL + "/health", String.class);
            return true;
        } catch (Exception e) {
            log.error("Go SMS Store service is unreachable: {}", e.getMessage());
            return false;
        }
    }
}