package com.example;

public class AccessControl {
    public static boolean isAllowed(String role, String query) {
        String queryType = query.trim().split("\\s+")[0].toUpperCase();
        
        if ("admin".equalsIgnoreCase(role)) {
            return true;
        } else if ("premium".equalsIgnoreCase(role)) {
            if (queryType.equals("DELETE") || queryType.equals("DROP")) {
                return false;
            }
            if ((queryType.equals("INSERT") || queryType.equals("UPDATE"))
                    && containsRestrictedTable(query, new String[]{"utente", "abbonamento", "genere", "metodo_di_pagamento"})) {
                return false;
            }
            return true;
        } else if ("free".equalsIgnoreCase(role)) {
            if (queryType.equals("SELECT")) {
                return query.toLowerCase().contains("contenuto");
            }
            return false;
        }
        return false;
    }

    private static boolean containsRestrictedTable(String query, String[] restrictedTables) {
        String lowerCaseQuery = query.toLowerCase();
        for (String table : restrictedTables) {
            if (lowerCaseQuery.contains(table.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}