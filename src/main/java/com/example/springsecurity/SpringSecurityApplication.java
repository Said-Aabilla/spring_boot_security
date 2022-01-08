package com.example.springsecurity;

import com.example.springsecurity.utility.JWTTokenProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;

import static com.example.springsecurity.constants.FileConstant.USER_FOLDER;

@SpringBootApplication
public class SpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityApplication.class, args);
        new File(USER_FOLDER).mkdirs();
    }

    @Bean
    public JWTTokenProvider jwtTokenProvider(){
        return new JWTTokenProvider();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
