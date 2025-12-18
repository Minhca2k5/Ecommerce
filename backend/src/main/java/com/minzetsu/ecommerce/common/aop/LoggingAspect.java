package com.minzetsu.ecommerce.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.minzetsu.ecommerce..service..*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("Start: {}", method);
        try {
            Object result = joinPoint.proceed();
            log.info("End: {} ({} ms)", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("Exception in {}: {}", method, ex.getMessage(), ex);
            throw ex;
        }
    }
}
