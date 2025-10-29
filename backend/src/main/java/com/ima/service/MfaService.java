package com.ima.service;


import com.ima.entity.User;
import com.ima.exception.CustomException;
import com.ima.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserRepository userRepository;

    @Value("${mfa.issuer}")
    private String issuer;

    public String generateSecretKey() {
        dev.samstevens.totp.secret.SecretGenerator secretGenerator =
                new dev.samstevens.totp.secret.DefaultSecretGenerator();
        return secretGenerator.generate();
    }

    public String getQRCodeUrl(String username, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secret, issuer
        );
    }

    public boolean verifyCode(String secret, String code) {
        dev.samstevens.totp.time.TimeProvider timeProvider =
                new dev.samstevens.totp.time.SystemTimeProvider();
        dev.samstevens.totp.code.CodeGenerator codeGenerator =
                new dev.samstevens.totp.code.DefaultCodeGenerator();
        dev.samstevens.totp.code.CodeVerifier verifier =
                new dev.samstevens.totp.code.DefaultCodeVerifier(codeGenerator, timeProvider);

        return verifier.isValidCode(secret, code);
    }

    public void enableMfa(String username, String secret) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new CustomException("ERR_USER_001", "User not found"));

        user.setMfaEnabled(true);
        user.setMfaSecret(secret);
        userRepository.save(user);
    }

    public void disableMfa(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new CustomException("ERR_USER_001", "User not found"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }
}
