package com.example;

import java.io.*;
import java.net.*;

public class DatabaseProxy {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private final String role;

    public DatabaseProxy(String role) {
        this.role = role;
    }

    public String executeQuery(String query) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.writeObject(role);
            output.writeObject(query);

            return (String) input.readObject();
        } catch (Exception e) {
            return "Error communicating with the server:" + e.getMessage();
        }
    }
}
