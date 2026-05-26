package com.sms.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.model.SmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SmsEventProducer {

    private static final Logger log = LoggerFactory.getLogger(SmsEventProducer.class);
    private static final String TOPIC = "sms-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishSmsEvent(SmsEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.getPhoneNumber(), json);
            log.info("Published event to Kafka: {}", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SMS event", e);
        }
    }
}
