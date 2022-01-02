package com.example.springsecurity.service;

public interface LoginAttemptService {

    void evictUserFromLoginAttemptCache(String username);

    void addUserToLoginAttemptCache(String username) ;

    boolean hasExceededLoginAttempts(String username);
}
