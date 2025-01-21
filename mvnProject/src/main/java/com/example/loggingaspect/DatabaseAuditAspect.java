package com.example.loggingaspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.File;

@Aspect
public class DatabaseAuditAspect {
    private final Logger logger;
    private final FileHandler fileHandler;
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "database_audit.log";

    public DatabaseAuditAspect() {
        this.logger = Logger.getLogger("DatabaseAudit");
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Create FileHandler with full path
            this.fileHandler = new FileHandler(LOG_DIR + File.separator + LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            
            // Log initialization success
            logger.info("Audit logging initialized at " + LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize audit logger", e);
        }
    }

    @Around("execution(* com.example.DatabaseServer.ClientHandler.handleAuthentication())")
    public Object logAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        String clientId = (String) target.getClass().getField("clientId").get(target);
        String email = null;
        boolean success = false;

        try {
            // Get input stream to read authentication parameters
            Object inputStream = target.getClass().getField("input").get(target);
            // Read email (we need to read this before proceed because the stream will be consumed)
            email = (String) inputStream.getClass().getMethod("readObject").invoke(inputStream);
            // Skip password read as we don't want to log it
            inputStream.getClass().getMethod("readObject").invoke(inputStream);

            Object result = joinPoint.proceed();
            success = ((String) result).startsWith("Authentication successful");
            return result;
        } finally {
            logger.info(String.format("[%s] Authentication attempt - Client: %s, User: %s, Success: %s",
                LocalDateTime.now(), clientId, email != null ? email : "unknown", success));
        }
    }

    @Around("execution(* com.example.DatabaseServer.ClientHandler.handleQuery())")
    public Object logQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Object inputStream = target.getClass().getField("input").get(target);
        String sessionId = null;
        String query = null;
        boolean success = false;

        try {
            // Read parameters before proceeding
            sessionId = (String) inputStream.getClass().getMethod("readObject").invoke(inputStream);
            query = (String) inputStream.getClass().getMethod("readObject").invoke(inputStream);

            Object result = joinPoint.proceed();
            success = !((String) result).startsWith("Access denied") && 
                    !((String) result).startsWith("Query execution error") &&
                    !((String) result).startsWith("Session expired");
            return result;
        } finally {
            logger.info(String.format("[%s] Query execution - Session: %s, Query: %s, Success: %s",
                LocalDateTime.now(), 
                sessionId != null ? sessionId : "unknown", 
                query != null ? query : "unknown", 
                success));
        }
    }

    public void cleanup() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}