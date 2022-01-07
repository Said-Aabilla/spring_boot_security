package com.example.springsecurity.service.impl;

import com.example.springsecurity.service.facade.LoginAttemptService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
    private static final int ATTEMPTS_INCREMENT = 1;
    private LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptServiceImpl() {
        super();
        this.loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    @Override
    public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username);
    }

    @Override
    public void addUserToLoginAttemptCache(String username)  {
        int attempts = 0;
        try {
            attempts = ATTEMPTS_INCREMENT + loginAttemptCache.get(username);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        loginAttemptCache.put(username, attempts);
    }

    @Override
    public boolean hasExceededLoginAttempts(String username)  {
        try {
            return loginAttemptCache.get(username) >= MAX_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
