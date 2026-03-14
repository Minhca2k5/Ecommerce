package com.minzetsu.ecommerce.common.audit.aop;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
import com.minzetsu.ecommerce.common.audit.service.AuditLogService;
import com.minzetsu.ecommerce.common.audit.entity.AuditLog;



@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {
    private static final Pattern SECRET_KV_PATTERN = Pattern.compile(
            "(?i)\\b(password|pass|pwd|token|secret|api[-_\\s]?key|authorization|otp)\\b\\s*[:=]\\s*([^,;\\s]+)"
    );
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._\\-]+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)\\b([a-z0-9._%+-]+)@([a-z0-9.-]+\\.[a-z]{2,})\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\+?\\d{9,15}\\b");

    private final AuditLogService auditLogService;

    @Around("@annotation(com.minzetsu.ecommerce.common.audit.entity.AuditAction)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuditAction auditAction = method.getAnnotation(AuditAction.class);

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(auditAction.action());
        auditLog.setEntityType(auditAction.entityType());
        auditLog.setUserId(resolveUserId());
        auditLog.setIpAddress(resolveIp());
        auditLog.setUserAgent(resolveUserAgent());

        try {
            Object result = joinPoint.proceed();
            auditLog.setSuccess(true);
            auditLog.setEntityId(resolveEntityId(joinPoint.getArgs(), auditAction.idParamIndex(), result));
            auditLogService.save(auditLog);
            return result;
        } catch (Throwable ex) {
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(trimError(ex.getMessage()));
            auditLog.setEntityId(resolveEntityId(joinPoint.getArgs(), auditAction.idParamIndex(), null));
            auditLogService.save(auditLog);
            throw ex;
        }
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private Long resolveEntityId(Object[] args, int idParamIndex, Object result) {
        if (idParamIndex >= 0 && args != null && idParamIndex < args.length) {
            Object arg = args[idParamIndex];
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof Number) {
                return ((Number) arg).longValue();
            }
        }
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object value = getId.invoke(result);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            } catch (Exception ignored) {
                // fallback to null
            }
        }
        return null;
    }

    private String resolveIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            return parts[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveUserAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String trimError(String message) {
        if (message == null) {
            return null;
        }
        String masked = maskSensitive(message);
        if (masked.length() <= 1000) {
            return masked;
        }
        return masked.substring(0, 1000);
    }

    private String maskSensitive(String input) {
        String masked = SECRET_KV_PATTERN.matcher(input).replaceAll("$1=***");
        masked = BEARER_PATTERN.matcher(masked).replaceAll("Bearer ***");
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("***@$2");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("***");
        return masked;
    }
}







