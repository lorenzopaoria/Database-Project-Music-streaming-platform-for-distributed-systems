package com.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DatabaseClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try (Socket socket = new Socket("127.0.0.1", 12345)) {
            // Create input stream first
            InputStream is = socket.getInputStream();
            ObjectInputStream input = new ObjectInputStream(is);

            // Then create output stream
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.flush(); // Flush the stream header

            System.out.println("Inserisci l'email: ");
            String email = scanner.nextLine();
            System.out.println("Inserisci la password: ");
            String password = scanner.nextLine();

            // Send authentication credentials
            output.writeObject(email);
            output.writeObject(password);
            output.flush();

            // Read authentication response
            String response = (String) input.readObject();
            System.out.println(response);

            if (response.startsWith("Authentication successful")) {
                while (true) {
                    System.out.println("Inserisci la query SQL o 'exit' per uscire:");
                    String query = scanner.nextLine();
                    
                    if ("exit".equalsIgnoreCase(query)) {
                        output.writeObject(query);
                        output.flush();
                        System.out.println("Uscita...");
                        break;
                    }

                    output.writeObject(query);
                    output.flush();

                    String result = (String) input.readObject();
                    System.out.println("Risultato della query:\n" + result);
                }                
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore nella connessione al server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}