package com.ima.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ima.entity.User;
import com.ima.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from OAuth provider
        String rawEmail = (String) oAuth2User.getAttributes().get("email");
        String login = (String) oAuth2User.getAttributes().get("login");
        String name = (String) oAuth2User.getAttributes().getOrDefault("name", login);

        if (rawEmail == null || rawEmail.isBlank()) {
            rawEmail = login + "@github.local"; // fallback for GitHub users without public email
        }
        final String email = rawEmail;

        // Find or create user in DB
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            newUser.setProvider("GITHUB");
            newUser.setAccountEnabled(true);
            return userRepository.save(newUser);
        });

        // Build Spring Security user
        UserDetails springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password("") // OAuth has no password
                .authorities("ROLE_USER")
                .build();

        // Generate access + refresh tokens
        String accessToken = jwtService.generateToken(springUser);
        String refreshToken = jwtService.generateRefreshToken(springUser);

        // Save refresh token in DB (optional, for invalidation)
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // Send refresh token as secure HttpOnly cookie
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // set true in production (HTTPS)
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        // Redirect to frontend with access token in query param
        String redirectUrl = String.format(
                "http://localhost:3000/auth/callback?token=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
        );

        response.sendRedirect(redirectUrl);
    }
}
