package com.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Value("${app.password}")
    private String appPassword;

    @PostMapping("/login")
    public String doLogin(@RequestParam("password") String password, HttpSession session) {
        if (appPassword.equals(password)) {
            session.setAttribute("authenticated", true);
            return "redirect:/";
        } else {
            return "redirect:/login.html?error=1";
        }
    }

    @GetMapping("/logout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login.html";
    }
}