package com.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/piattaforma_streaming_musicale";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Method to establish a database connection
    public static Connection getConnection() throws SQLException {
        try {
            // Registra il driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Stabilisci la connessione
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trovato", e);
        }
    }

    // Metodo per chiudere la connessione (opzionale)
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Errore durante la chiusura della connessione: " + e.getMessage());
            }
        }
    }
}