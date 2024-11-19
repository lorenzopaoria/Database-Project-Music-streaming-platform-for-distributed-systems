package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseServer {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/piattaforma_streaming_musicale";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void startServer() {
        try {
            // Inizializza la connessione al database
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connessione al database avvenuta con successo!");

            // Il server resta in attesa di nuove richieste (simulato con un ciclo)
            while (true) {
                // Qui potresti aggiungere un ciclo infinito per gestire richieste
                // Può essere una gestione di richieste a un server o anche solo per rimanere attivo
            }
        } catch (SQLException e) {
            System.out.println("Errore durante la connessione al database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
