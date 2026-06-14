package com.police.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.police.common.annotation.OperationLog;
import com.police.common.util.SecurityUtil;
import com.police.system.entity.SysOperationLog;
import com.police.system.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint pjp, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Integer responseCode = 200;

        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            responseCode = 500;
            throw e;
        } finally {
            try {
                long executeTime = System.currentTimeMillis() - startTime;
                saveLog(pjp, operationLog, responseCode, executeTime);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint pjp, OperationLog annotation,
                         Integer responseCode, long executeTime) {
        HttpServletRequest request = getRequest();
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setUserId(SecurityUtil.getCurrentUserId());
        logEntity.setUserName(SecurityUtil.getCurrentRealName());
        logEntity.setModule(annotation.module());
        logEntity.setAction(annotation.action());
        logEntity.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        logEntity.setResponseCode(responseCode);
        logEntity.setExecuteTime((int) executeTime);
        logEntity.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestIp(getClientIp(request));
        }

        if (annotation.logBody()) {
            try {
                Object[] args = pjp.getArgs();
                String body = objectMapper.writeValueAsString(args);
                // 截断过长内容，避免超出字段长度
                if (body.length() > 2000) {
                    body = body.substring(0, 2000) + "...(truncated)";
                }
                logEntity.setRequestBody(body);
            } catch (Exception ignored) {}
        }

        operationLogMapper.insert(logEntity);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
