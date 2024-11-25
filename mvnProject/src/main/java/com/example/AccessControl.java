package com.example;

public class AccessControl {
    public static boolean isAllowed(String role, String query) {
        String queryType = query.trim().split("\\s+")[0].toUpperCase();

        if ("admin".equalsIgnoreCase(role)) {
            return true; // Admin può fare tutto
        } else if ("premium".equalsIgnoreCase(role)) {
            return !queryType.equals("DELETE"); // Premium non può fare DELETE
        } else if ("free".equalsIgnoreCase(role)) {
            return queryType.equals("SELECT"); // Free solo SELECT
        }
        return false;
    }
}
