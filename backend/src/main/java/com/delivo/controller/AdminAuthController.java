package com.delivo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth/admin")
public class AdminAuthController {
    @GetMapping("/login")
    public String login() {
        return "auth/admin-login";
    }
}
