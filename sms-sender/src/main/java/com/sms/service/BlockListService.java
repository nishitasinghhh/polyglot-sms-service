package com.sms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlockListService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLOCKED_USERS_KEY = "blocked_users";

    public boolean isBlocked(String phoneNumber) {
        Boolean result = redisTemplate.opsForSet().isMember(BLOCKED_USERS_KEY, phoneNumber);
        return Boolean.TRUE.equals(result);
    }

    public void blockUser(String phoneNumber) {
        redisTemplate.opsForSet().add(BLOCKED_USERS_KEY, phoneNumber);
    }
}
