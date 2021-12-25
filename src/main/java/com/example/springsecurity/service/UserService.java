package com.example.springsecurity.service;

import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;

import java.util.List;

public interface UserService {
    User register(String firstName,String lastName, String username, String email) throws EmailExistException, UsernameExistException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
