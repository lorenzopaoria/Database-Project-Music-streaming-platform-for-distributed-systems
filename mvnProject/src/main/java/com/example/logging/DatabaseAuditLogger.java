package com.example.logging;

import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DatabaseAuditLogger {
    private final Logger logger;

    public DatabaseAuditLogger() {
        this.logger = Logger.getLogger("DatabaseAudit");
        try {
            FileHandler fh = new FileHandler("database_audit.log", true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize audit logger", e);
        }
    }

    public void logAuthentication(String clientId, String email, boolean success) {
        logger.info(String.format("[%s] Authentication attempt - Client: %s, User: %s, Success: %s",
            LocalDateTime.now(), clientId, email, success));
    }

    public void logQuery(String sessionId, String query, boolean success) {
        logger.info(String.format("[%s] Query execution - Session: %s, Query: %s, Success: %s",
            LocalDateTime.now(), sessionId, query, success));
    }
}