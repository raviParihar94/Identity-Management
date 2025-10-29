package com.ima.security;

import com.ima.service.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {


    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String username;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User oAuth2User) {
            username = oAuth2User.getEmail(); // or oAuth2User.getUsername()
        } else {
            username = authentication.getName();
        }

        // ✅ Load full UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ✅ Generate JWT tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Example: redirect or return tokens
        String redirectUrl = String.format(
                "http://localhost:3000/login/success?access=%s&refresh=%s",
                accessToken, refreshToken
        );
        response.sendRedirect(redirectUrl);
    }

}
