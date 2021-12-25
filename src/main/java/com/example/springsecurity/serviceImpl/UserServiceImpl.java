package com.example.springsecurity.serviceImpl;

import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import com.example.springsecurity.domain.UserPrincipal;
import com.example.springsecurity.repository.UserRepository;
import com.example.springsecurity.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.example.springsecurity.enumeration.Role.ROLE_USER;
import static org.apache.commons.lang3.StringUtils.EMPTY;


@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    //to show the error on the logger you will need this:
    private Logger LOGGER = LoggerFactory.getLogger(getClass());


    @Autowired
    public UserServiceImpl(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            //log it on the console
            LOGGER.error("User not found by username: " + username);
            //throw exception
            throw new UsernameNotFoundException("User not found by username: " + username);
        } else {
            //if the user is found we want to update it first
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            //then return the UserDetails
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning user found by username: " + username);
            return userPrincipal;
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email) throws EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(EMPTY, username,email);
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getUserAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl());
        LOGGER.info("user password: "+ password);
        userRepository.save(user);
        return user;
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UsernameExistException, EmailExistException {
        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UsernameNotFoundException("No user found by username: " + currentUsername);
            }

            User userByNewUsername = findUserByUsername(newUsername);
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException("Username already exists!");
            }

            User userByNewEmail = findUserByEmail(newEmail);
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException("Email already exists!");
            }
            return currentUser;
        } else {
            User userByUsername = findUserByUsername(newUsername);
            if (userByUsername != null) {
                throw new UsernameExistException("Username already exists!");
            }
            User userByEmail = findUserByEmail(newEmail);
            if (userByEmail != null ) {
                throw new EmailExistException("Email already exists!");
            }
        }
        return null;
    }

    @Override
    public List<User> getUsers() {
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return null;
    }
}
