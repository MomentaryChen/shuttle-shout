package com.shuttleshout.aspect;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Controller日誌切面
 * 記錄所有Controller方法的調用資訊
 * 
 * @author ShuttleShout Team
 */
@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    /**
     * 建構函數：初始化時輸出日誌
     */
    public ControllerLoggingAspect() {
        logger.info("ControllerLoggingAspect 已初始化，AOP切面已啟用");
    }

    /**
     * 定義切點：攔截所有Controller包下的方法
     */
    @Pointcut("execution(* com.shuttleshout.controller..*(..))")
    public void controllerMethods() {
    }

    /**
     * 環繞通知：記錄Controller方法的調用資訊
     * 
     * @param joinPoint 連接點
     * @return 方法執行結果
     * @throws Throwable 異常
     */
    @Around("controllerMethods()")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 獲取請求資訊
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 獲取方法資訊
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 建構日誌資訊
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== Controller調用開始 ==========");
        logMessage.append("\n類名: ").append(className);
        logMessage.append("\n方法名: ").append(methodName);

        if (request != null) {
            logMessage.append("\nHTTP方法: ").append(request.getMethod());
            logMessage.append("\n請求路徑: ").append(request.getRequestURI());
            logMessage.append("\n客戶端IP: ").append(getClientIpAddress(request));
        }

        logMessage.append("\n參數: ");
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                logMessage.append("\n  [").append(i).append("] ");
                if (args[i] != null) {
                    // 對於複雜物件，只顯示類名和toString的前100個字元
                    String argStr = args[i].toString();
                    if (argStr.length() > 100) {
                        argStr = argStr.substring(0, 100) + "...";
                    }
                    logMessage.append(args[i].getClass().getSimpleName()).append(": ").append(argStr);
                } else {
                    logMessage.append("null");
                }
            }
        } else {
            logMessage.append("無參數");
        }

        // 記錄開始時間
        long startTime = System.currentTimeMillis();
        logMessage.append("\n開始時間: ").append(new java.util.Date(startTime));
        logMessage.append("\n==========================================");

        logger.info(logMessage.toString());

        try {
            // 執行方法
            Object result = joinPoint.proceed();

            // 計算執行時間
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 記錄返回結果
            StringBuilder resultLog = new StringBuilder();
            resultLog.append("\n========== Controller調用結束 ==========");
            resultLog.append("\n類名: ").append(className);
            resultLog.append("\n方法名: ").append(methodName);
            resultLog.append("\n執行時間: ").append(executionTime).append(" ms");
            resultLog.append("\n返回結果: ");

            if (result != null) {
                String resultStr = result.toString();
                if (resultStr.length() > 200) {
                    resultStr = resultStr.substring(0, 200) + "...";
                }
                resultLog.append(result.getClass().getSimpleName()).append(": ").append(resultStr);
            } else {
                resultLog.append("null");
            }

            resultLog.append("\n結束時間: ").append(new java.util.Date(endTime));
            resultLog.append("\n==========================================");
            logger.info(resultLog.toString());

            return result;

        } catch (Throwable e) {
            // 記錄異常
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            StringBuilder errorLog = new StringBuilder();
            errorLog.append("\n========== Controller調用異常 ==========");
            errorLog.append("\n類名: ").append(className);
            errorLog.append("\n方法名: ").append(methodName);
            errorLog.append("\n執行時間: ").append(executionTime).append(" ms");
            errorLog.append("\n異常類型: ").append(e.getClass().getName());
            errorLog.append("\n異常資訊: ").append(e.getMessage());
            // errorLog.append("\n異常堆疊: ").append(Arrays.toString(e.getStackTrace()));
            errorLog.append("\n==========================================");

            logger.error(errorLog.toString(), e);

            throw e;
        }
    }

    /**
     * 獲取客戶端IP位址
     * 
     * @param request HTTP請求
     * @return IP位址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
