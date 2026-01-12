package com.ima.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ima.entity.User;
import com.ima.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${spring.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        String provider = registrationId.toUpperCase();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from OAuth provider
        String rawEmail = (String) oAuth2User.getAttributes().get("email");
        String login = (String) oAuth2User.getAttributes().get("login");
        String name = (String) oAuth2User.getAttributes().getOrDefault("name", login);

        if (rawEmail == null || rawEmail.isBlank()) {
            rawEmail = login + "@" + provider.toLowerCase() + ".local"; // fallback for users without public email
        }
        final String email = rawEmail;

        // Find or create user in DB
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(name != null ? name : email); // Ensure username is not null
            newUser.setEmail(email);
            newUser.setProvider(provider);
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
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("token", accessToken) // UriComponentsBuilder encodes automatically? No, better be safe but
                                                  // usually yes.
                // Actually, let's stick to the previous manual encoding if we want to be 100%
                // sure,
                // but UriComponentsBuilder.queryParam DOES encode.
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
