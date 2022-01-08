package com.example.springsecurity.service.impl;

import com.example.springsecurity.domain.UserPrincipal;
import com.example.springsecurity.enumeration.Role;
import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.EmailNotFoundException;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import com.example.springsecurity.repository.UserRepository;
import com.example.springsecurity.service.facade.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.springsecurity.constants.FileConstant.*;
import static com.example.springsecurity.constants.UserImplConstants.*;
import static com.example.springsecurity.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;


@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LoginAttemptServiceImpl loginAttemptService;
    private EmailService emailService;
    //to show the error on the logger you will need this:
    private Logger LOGGER = LoggerFactory.getLogger(getClass());


    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           LoginAttemptServiceImpl loginAttemptService,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            //log it on the console
            LOGGER.error(NO_USER_FOUND_BY_USERNAME + username);
            //throw exception
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        } else {
            validateLoginAttempt(user);
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
    public User register(User user) throws EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(EMPTY, user.getUsername(), user.getEmail());
        User registerUser = new User();
        registerUser.setUserId(generateUserId());
        String password = generatePassword();
        registerUser.setPassword(encodePassword(password));
        registerUser.setFirstname(user.getFirstname());
        registerUser.setLastname(user.getLastname());
        registerUser.setUsername(user.getUsername());
        registerUser.setEmail(user.getEmail());
        registerUser.setJoinDate(new Date());
        registerUser.setActive(true);
        registerUser.setNotLocked(true);
        registerUser.setRole(ROLE_USER.name());
        registerUser.setAuthorities(ROLE_USER.getUserAuthorities());
        registerUser.setProfileImageUrl(getTemporaryProfileImageUrl(user.getUsername()));
        //emailService.sendNewPasswordEmail(user.getFirstname(), password,user.getEmail());
        userRepository.save(registerUser);
        LOGGER.info("new user password : "+ password);
        return registerUser;
    }


    @Override
    public User addNewUser(User user, MultipartFile profileImage) throws EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(EMPTY, user.getUsername(), user.getEmail());
        User newUser = new User();
        newUser.setUserId(generateUserId());
        String password = generatePassword();
        newUser.setPassword(encodePassword(password));
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setActive(user.isActive());
        newUser.setNotLocked(user.isNotLocked());
        newUser.setJoinDate(new Date());
        newUser.setRole(getRoleEnumName(user.getRole()).name());
        newUser.setAuthorities(getRoleEnumName(user.getRole()).getUserAuthorities());
        newUser.setProfileImageUrl(getTemporaryProfileImageUrl(user.getUsername()));
        //emailService.sendNewPasswordEmail(user.getFirstname(), password,user.getEmail());
        userRepository.save(newUser);
        saveProfileImage(newUser, profileImage);
        return newUser;
    }


    @Override
    public User updateUser(String currentUsername, User newUser, MultipartFile profileImage) throws EmailExistException, UsernameExistException {
        User currentUser = validateNewUsernameAndEmail(currentUsername, newUser.getUsername(), newUser.getEmail());
        currentUser.setUserId(generateUserId());
        currentUser.setUsername(newUser.getUsername());
        currentUser.setFirstname(newUser.getFirstname());
        currentUser.setLastname(newUser.getLastname());
        currentUser.setEmail(newUser.getEmail());
        currentUser.setRole(getRoleEnumName(newUser.getRole()).name());
        currentUser.setAuthorities(getRoleEnumName(newUser.getRole()).getUserAuthorities());
        currentUser.setActive(newUser.isActive());
        currentUser.setNotLocked(newUser.isNotLocked());
        //emailService.sendNewPasswordEmail(user.getFirstname(), password,user.getEmail());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        //emailService.sendNewPasswordEmail(user.getFirstname(), password,email);
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws EmailExistException, UsernameExistException {
        User user = validateNewUsernameAndEmail(username, null, null);
        saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }


    //HELPER Functions
    private void validateLoginAttempt(User user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.hasExceededLoginAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
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
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);

        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }

            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }

            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
        }
        return null;
    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
        if (profileImage != null) {
            try {
                Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
                if (!Files.exists(userFolder)) {
                    Files.createDirectories(userFolder);
                    LOGGER.info(DIRECTORY_CREATED + userFolder);
                }
                Files.deleteIfExists(Paths.get(USER_FOLDER + user.getUsername() + DOT + JPG_EXTENSION));
                Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
                user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
                userRepository.save(user);
                LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
                + username + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

}
