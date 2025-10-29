package com.ima.service;

import com.ima.entity.AuditLog;
import com.ima.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String username, String action, String screenId, String errorCode, String details) {
        String ipAddress = getClientIp();

        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .screenId(screenId)
                .errorCode(errorCode)
                .ipAddress(ipAddress)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "Unknown";
    }
}

