package com.example;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 1. Allow public paths (login page, logout, and static resources for login)
        if (path.startsWith("/login") || path.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Check if the session has an "authenticated" flag
        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null) && Boolean.TRUE.equals(session.getAttribute("authenticated"));

        if (isAuthenticated) {
            chain.doFilter(request, response);
        } else {
            // 3. Not logged in -> redirect to login page
            httpResponse.sendRedirect("/login.html");
        }
    }
}