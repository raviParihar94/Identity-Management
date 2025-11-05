package com.ima.service;

import com.ima.dto.AuthResponse;
import com.ima.dto.LoginRequest;
import com.ima.dto.MfaVerifyRequest;
import com.ima.dto.RegisterRequest;
import com.ima.entity.Role;
import com.ima.entity.User;
import com.ima.exception.CustomException;
import com.ima.repository.RoleRepository;
import com.ima.repository.UserRepository;
import com.ima.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MfaService mfaService;
    private final AuditLogService auditLogService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("ERR_REG_001", "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("ERR_REG_002", "Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .description("Default user role")
                            .permissions(new HashSet<>())
                            .build();
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .accountEnabled(true)
                .accountLocked(false)
                .mfaEnabled(false)
                .build();

        userRepository.save(user);

        auditLogService.log(user.getUsername(), "USER_REGISTERED", "SCR_REGISTER_001", null, null);

        var jwtToken = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_USER")
                        .build()
        );

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .mfaEnabled(false)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("ERR_AUTH_001", "Invalid credentials"));

        if (user.isAccountLocked()) {
            auditLogService.log(user.getUsername(), "LOGIN_FAILED_LOCKED", "SCR_LOGIN_001",
                    "ERR_AUTH_002", null);
            throw new CustomException("ERR_AUTH_002", "Account is locked");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            handleFailedLogin(user);
            auditLogService.log(user.getUsername(), "LOGIN_FAILED", "SCR_LOGIN_001",
                    "ERR_AUTH_001", null);
            throw new CustomException("ERR_AUTH_001", "Invalid credentials");
        }

        if (user.isMfaEnabled()) {
            auditLogService.log(user.getUsername(), "MFA_REQUIRED", "SCR_LOGIN_001", null, null);
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaEnabled(true)
                    .username(user.getUsername())
                    .build();
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse verifyMfaAndLogin(MfaVerifyRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("ERR_AUTH_001", "User not found"));

        if (!user.isMfaEnabled()) {
            throw new CustomException("ERR_MFA_001", "MFA is not enabled for this user");
        }

        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), request.getCode());

        if (!isValid) {
            auditLogService.log(user.getUsername(), "MFA_FAILED", "SCR_LOGIN_001",
                    "ERR_MFA_002", null);
            throw new CustomException("ERR_MFA_002", "Invalid MFA code");
        }

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        resetFailedLoginAttempts(user);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName())
                        .toArray(String[]::new))
                .build();

        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        auditLogService.log(user.getUsername(), "LOGIN_SUCCESS", "SCR_LOGIN_001", null, null);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .mfaEnabled(user.isMfaEnabled())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            auditLogService.log(user.getUsername(), "ACCOUNT_LOCKED", "SCR_LOGIN_001",
                    "ERR_AUTH_003", "Too many failed login attempts");
        }

        userRepository.save(user);
    }

    private void resetFailedLoginAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }
}