package com.example.springsecurity.serviceImpl;

import com.example.springsecurity.model.user.User;
import com.example.springsecurity.domain.UserPrincipal;
import com.example.springsecurity.repository.UserRepository;
import com.example.springsecurity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;


@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepository userRepository;
    //to show the error on the logger you will need this:
    private  Logger LOGGER = LoggerFactory.getLogger(getClass());


    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            //log it on the console
            LOGGER.error("User not found by username: " + username);
            //throw exception
            throw new UsernameNotFoundException("User not found by username: " + username);
        }else{
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
}
