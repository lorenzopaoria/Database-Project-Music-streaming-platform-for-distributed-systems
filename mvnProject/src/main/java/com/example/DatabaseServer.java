package com.example;

import java.io.*;
import java.net.*;
import java.sql.*;
import com.example.DatabaseConnection;



public class DatabaseServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) { // Porta 12345
            System.out.println("Server in ascolto sulla porta 12345...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da: " + clientSocket.getInetAddress());
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true)
        ) {
            String query = reader.readLine(); // Legge la query inviata dal client
            try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    writer.println(rs.getString(1)); // Stampa il risultato della query
                }
            } catch (SQLException e) {
                writer.println("Errore durante l'esecuzione della query: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
