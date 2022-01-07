package com.example.springsecurity.resource;

import com.example.springsecurity.domain.UserPrincipal;
import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.ExceptionHandling;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import com.example.springsecurity.service.facade.UserService;
import com.example.springsecurity.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.springsecurity.constants.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(  value = "/user")
public class UserResource extends ExceptionHandling {

    private UserService userService;
    //Auth helpers
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserResource(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping( "/register")
    public ResponseEntity<User> register(@RequestBody User user) throws EmailExistException, UsernameExistException {
       User newUser = userService.register(user);
       return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser , jwtHeader,OK);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return  headers;
    }
}
