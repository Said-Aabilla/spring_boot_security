package com.example.springsecurity.resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(  value = "/user")
public class UserResource {
    @GetMapping(value = "/home")
    public String showUser(){
        return "app works fine!";
    }
}
