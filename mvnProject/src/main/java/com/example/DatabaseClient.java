package com.example;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class DatabaseClient {
    public static void main(String[] args) {
        /*Scanner scanner = new Scanner(System.in);
        
        try (Socket socket = new Socket("127.0.0.1", 12345)) {
            InputStream is = socket.getInputStream();
            ObjectInputStream input = new ObjectInputStream(is);

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.flush();

            System.out.println("Insert email: ");
            String email = scanner.nextLine();
            System.out.println("Insert password: ");
            String password = scanner.nextLine();

            output.writeObject(email);
            output.writeObject(password);
            output.flush();

            String response = (String) input.readObject();
            System.out.println(response);

            if (response.startsWith("Authentication successful")) {
                while (true) {
                    System.out.println("Enter SQL query or 'exit' to exit:");
                    String query = scanner.nextLine();
                    
                    if ("exit".equalsIgnoreCase(query)) {
                        output.writeObject(query);
                        output.flush();
                        System.out.println("Exit...");
                        break;
                    }

                    output.writeObject(query);
                    output.flush();

                    String result = (String) input.readObject();
                    System.out.println("Query result:\n" + result);
                }                
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error connecting to the server:" + e.getMessage());
            e.printStackTrace();
        }
    }*/
        List<String[]> credentials = List.of(
            new String[]{"annapistorio@gmail.com", "anna04"},
            new String[]{"margheritaursino@gmail.com", "marghe02"}
        );

        List<String> testQueries = List.of(
                "SELECT * FROM contenuto;",
                "INSERT INTO playlist (email, nomePlaylist, num_tracce_P) VALUES ('margheritaursino@gmail.com', 'John Doe', 12);",
                "UPDATE playlist SET num_tracce_P = 13 WHERE nomePlaylist = 'John Doe';",
                "DELETE FROM playlist WHERE nomePlaylist = 'John Doe';"
        );

        for (String[] credential : credentials) {
            String email = credential[0];
            String password = credential[1];

            System.out.println("\n=== Testing with email: " + email + " ===");
            try (Socket socket = new Socket("127.0.0.1", 12345)) {
                InputStream is = socket.getInputStream();
                ObjectInputStream input = new ObjectInputStream(is);

                OutputStream os = socket.getOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(os);
                output.flush();

                output.writeObject(email);
                output.writeObject(password);
                output.flush();

                String response = (String) input.readObject();
                System.out.println(response);

                if (response.startsWith("Authentication successful")) {
                    for (String query : testQueries) {
                        System.out.println("Executing query: " + query);
                        output.writeObject(query);
                        output.flush();

                        String result = (String) input.readObject();
                        System.out.println("Query result:\n" + result);
                    }

                    output.writeObject("exit");
                    output.flush();
                    System.out.println("Exit...");
                } else {
                    System.out.println("Authentication failed for: " + email);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error connecting to the server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}