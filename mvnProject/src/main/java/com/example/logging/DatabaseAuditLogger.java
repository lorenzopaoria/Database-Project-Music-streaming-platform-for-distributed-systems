package com.example.logging;

import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseAuditLogger {
    private static final Logger LOGGER = Logger.getLogger(DatabaseAuditLogger.class.getName());

    public void logAuthentication(String email, boolean success) {
        String status = success ? "successful" : "failed";
        LOGGER.info(String.format("Authentication %s for email: %s", status, email));
    }

    public void logQueryExecution(String userId, String query, boolean success) {
        String status = success ? "successful" : "failed";
        LOGGER.info(String.format("Query %s - User: %s, Query: %s", status, userId, query));
    }
}