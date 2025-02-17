package com.example.logging;

import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DatabaseAuditLogger {
    private static DatabaseAuditLogger instance;
    private final Logger logger;
    private FileHandler fileHandler;

    private DatabaseAuditLogger() {
        this.logger = Logger.getLogger("DatabaseAudit");
        try {
            fileHandler = new FileHandler("database_audit.log", 1024 * 1024, 1, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize audit logger", e);
        }
    }

    public static synchronized DatabaseAuditLogger getInstance() {
        if (instance == null) {
            instance = new DatabaseAuditLogger();
        }
        return instance;
    }

    public void logAuthentication(String clientId, String email, String tipoUtente, boolean success) {
        logger.info(String.format("[%s] Authentication attempt - Client: %s, User: %s, Tipo Utente: %s, Success: %s",
            LocalDateTime.now(), clientId, email, tipoUtente, success));
    }

    public void logQuery(String sessionId, String query, boolean success) {
        logger.info(String.format("[%s] Query execution - Session: %s, Query: %s, Success: %s",
            LocalDateTime.now(), sessionId, query, success));
    }

    public void closeLogger() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}