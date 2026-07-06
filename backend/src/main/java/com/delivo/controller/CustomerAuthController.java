package com.delivo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth/customer")
public class CustomerAuthController {
    @GetMapping("/login")
    public String login() {
        return "auth/customer-login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/customer-register";
    }
}
