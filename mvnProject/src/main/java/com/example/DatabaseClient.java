package com.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DatabaseClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try (Socket socket = new Socket("127.0.0.1", 12345)) {
            // input stream first
            InputStream is = socket.getInputStream();
            ObjectInputStream input = new ObjectInputStream(is);

            // create output stream
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.flush(); // Flush the stream header

            System.out.println("Insert email: ");
            String email = scanner.nextLine();
            System.out.println("Insert password: ");
            String password = scanner.nextLine();

            // send authentication credentials
            output.writeObject(email);
            output.writeObject(password);
            output.flush(); // thread

            // read authentication response
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
    }
}