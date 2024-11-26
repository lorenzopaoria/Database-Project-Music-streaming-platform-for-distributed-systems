package com.example;

public class AccessControl {

    public static boolean isAllowed(String role, String query) {
        // determina il tipo di query (es: SELECT, DELETE, ecc.)
        String queryType = query.trim().split("\\s+")[0].toUpperCase();

        if ("admin".equalsIgnoreCase(role)) {
            // gli admin possono fare qualsiasi operazione
            return true;
        } else if ("premium".equalsIgnoreCase(role)) {
            // gli utenti premium:non possono fare DELETE e DROP, non possono fare INSERT o UPDATE sulle tabelle specifiche
            if (queryType.equals("DELETE") || queryType.equals("DROP")) {
                return false;
            }
            if ((queryType.equals("INSERT") || queryType.equals("UPDATE")) 
                    && containsRestrictedTable(query, new String[]{"utente", "abbonamento", "genere", "metodo_di_pagamento"})) {
                return false;
            }
            return true; 
        } else if ("free".equalsIgnoreCase(role)) {
            // gli utenti free possono fare solo SELECT, consentito solo se riguarda i brani
            if (queryType.equals("SELECT")) {
                return query.toLowerCase().contains("brani");
            }
            return false;
        }

        // ruolo non riconosciuto, accesso negato
        return false;
    }

    /**
     * Verifica se una query contiene riferimenti a tabelle riservate.
     * @param query La query da analizzare.
     * @param restrictedTables Elenco delle tabelle riservate.
     * @return true se la query contiene una delle tabelle riservate, false altrimenti.
     */
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
