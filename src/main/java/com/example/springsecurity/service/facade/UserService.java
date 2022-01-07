package com.example.springsecurity.service.facade;

import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User register(User user) throws EmailExistException, UsernameExistException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
