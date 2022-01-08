package com.example.springsecurity.resource;

import com.example.springsecurity.domain.HttpResponse;
import com.example.springsecurity.domain.UserPrincipal;
import com.example.springsecurity.exception.domain.EmailExistException;
import com.example.springsecurity.exception.domain.EmailNotFoundException;
import com.example.springsecurity.exception.domain.ExceptionHandling;
import com.example.springsecurity.exception.domain.UsernameExistException;
import com.example.springsecurity.model.user.User;
import com.example.springsecurity.service.facade.UserService;
import com.example.springsecurity.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.example.springsecurity.constants.FileConstant.*;
import static com.example.springsecurity.constants.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Controller
@RequestMapping(value = "/user")
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

    @PostMapping("/register")
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
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping(path = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<User> addNewUser(@RequestParam String firstname,
                                           @RequestParam String lastname,
                                           @RequestParam String email,
                                           @RequestParam String role,
                                           @RequestParam String isActive,
                                           @RequestParam String isNonLocked,
                                           @RequestParam String username,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailExistException, UsernameExistException {
        User user = setUserParams(firstname, lastname, username, email, role, isActive, isNonLocked);
        User newUser = userService.addNewUser(user, profileImage);
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping(path ="/update", consumes = {"multipart/form-data"})
    public ResponseEntity<User> updateUser(@RequestParam String currentUsername,
                                           @RequestParam String firstname,
                                           @RequestParam String lastname,
                                           @RequestParam String email,
                                           @RequestParam String role,
                                           @RequestParam String isActive,
                                           @RequestParam String isNonLocked,
                                           @RequestParam String username,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UsernameExistException, EmailExistException {
        User user = setUserParams(firstname, lastname, username, email, role, isActive, isNonLocked);
        User updateUser = userService.updateUser(currentUsername, user, profileImage);
        return new ResponseEntity<>(updateUser, OK);
    }

    @PostMapping(path = "/updateProfileImage", consumes = {"multipart/form-data"})
    public ResponseEntity<User> updateProfileImage(@RequestParam String username, @RequestParam("profileImage") MultipartFile profileImage) throws EmailExistException, UsernameExistException {
        User updateUser = userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(updateUser, OK);
    }

    @DeleteMapping("/delete/{id}")
   // @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return response(NO_CONTENT, "User deleted !");
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable String email) throws EmailNotFoundException {
        userService.resetPassword(email);
        return response(OK, "Email with new password was sent to: " + email);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable String username, @PathVariable String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }


    @GetMapping("/find/{username}")
    public ResponseEntity<User> deleteUser(@PathVariable String username) {
        User foundUser = userService.findUserByUsername(username);
        return new ResponseEntity<>(foundUser, OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);

    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private ResponseEntity<HttpResponse> response(HttpStatus status, String message) {
        return new ResponseEntity<>(new HttpResponse(status.value(), status, status.getReasonPhrase().toUpperCase(), message.toUpperCase()), status);
    }

    private User setUserParams(String firstname, String lastname, String username, String email, String role, String isActive, String isNonLocked) {
        User user = new User();
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setNotLocked(Boolean.parseBoolean(isNonLocked));
        user.setActive(Boolean.parseBoolean(isActive));
        return user;
    }

}
