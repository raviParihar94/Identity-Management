package com.ima.service;

import com.ima.entity.AuditLog;
import com.ima.entity.Screen;
import com.ima.entity.User;
import com.ima.exception.CustomException;
import com.ima.repository.AuditLogRepository;
import com.ima.repository.ScreenRepository;
import com.ima.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreenAccessService {

    private final ScreenRepository screenRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public boolean hasAccess(String username, String screenId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("ERR_USER_001", "User not found"));

        Screen screen = screenRepository.findByScreenId(screenId)
                .orElseThrow(() -> new CustomException("ERR_SCREEN_001", "Screen not found"));

        if (!screen.isActive()) {
            logAccess(user, screenId, "ACCESS_DENIED", "Screen is inactive");
            return false;
        }

        boolean hasAccess = user.getRoles().stream()
                .anyMatch(role -> screen.getAllowedRoles().contains(role));

        String action = hasAccess ? "ACCESS_GRANTED" : "ACCESS_DENIED";
        logAccess(user, screenId, action, null);

        return hasAccess;
    }

    public List<Screen> getAccessibleScreens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("ERR_USER_001", "User not found"));

        return screenRepository.findByIsActive(true).stream()
                .filter(screen -> user.getRoles().stream()
                        .anyMatch(role -> screen.getAllowedRoles().contains(role)))
                .collect(Collectors.toList());
    }

    private void logAccess(User user, String screenId, String action, String details) {
        AuditLog log = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action(action)
                .screenId(screenId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
