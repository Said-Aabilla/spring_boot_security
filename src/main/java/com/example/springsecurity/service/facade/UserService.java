package com.example.springsecurity.service.facade;

import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.EmailNotFoundException;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends UserDetailsService {
    User register(User user) throws EmailExistException, UsernameExistException;
    List<User> getUsers();
    User findUserByUsername(String username);
    User findUserByEmail(String email);
    User addNewUser(User user, MultipartFile profileImage) throws EmailExistException, UsernameExistException;
    User updateUser(String currentUsername,User newUser, MultipartFile profileImage) throws EmailExistException, UsernameExistException;
    void deleteUser(long id);
    void resetPassword(String email) throws EmailNotFoundException;
    User updateProfileImage(String username,MultipartFile profileImage) throws EmailExistException, UsernameExistException;
}
