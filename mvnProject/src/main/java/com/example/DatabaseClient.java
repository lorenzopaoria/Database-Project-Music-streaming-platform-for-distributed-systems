package com.example;

import java.sql.*;
import java.util.Scanner;

public class DatabaseClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Connessione al database all'inizio
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ciclo per permettere l'esecuzione di più query
            while (true) {
                System.out.println("Inserisci la query SQL (ad esempio: SELECT * FROM contenuti) o 'exit' per uscire:");
                String query = scanner.nextLine();

                // Uscita dal programma
                if (query.equalsIgnoreCase("exit")) {
                    System.out.println("Uscita dal programma...");
                    break;
                }
                
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(query);

                    // Get metadata to dynamically print column names and values
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Print column names
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf("%-20s", metaData.getColumnName(i));
                    }
                    System.out.println("\n" + "-".repeat(20 * columnCount));

                    // Print results
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.printf("%-20s", rs.getString(i));
                        }
                        System.out.println();
                    }

                    rs.close();  // Chiudere il ResultSet
                } catch (SQLException e) {
                    System.out.println("Errore durante l'esecuzione della query: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Errore durante la connessione al database: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
