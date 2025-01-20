package com.example.proxy;

import com.example.session.Session;
import java.io.*;
import java.net.Socket;

public class DatabaseProxy {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private Session session;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public DatabaseProxy() {
        connect();
    }

    private void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to server", e);
        }
    }

    public String authenticate(String email, String password) throws IOException, ClassNotFoundException {
        output.writeObject("AUTH");
        output.writeObject(email);
        output.writeObject(password);
        output.flush();
        
        String response = (String) input.readObject();
        if (response.startsWith("Authentication successful")) {
            String[] parts = response.split(":");
            if (parts.length == 2) {
                String sessionId = parts[1].trim();
                session = new Session(email, sessionId);
                return "Authentication successful";
            }
        }
        return response;
    }
    
    public String executeQuery(String query) throws IOException, ClassNotFoundException {
        if (session == null || session.isExpired()) {
            throw new SecurityException("Session expired");
        }
        
        output.writeObject("QUERY");
        output.writeObject(session.getSessionId());
        output.writeObject(query);
        output.flush();
        
        return (String) input.readObject();
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                output.writeObject("EXIT");
                output.flush();
                socket.close();
            }
        } catch (IOException e) {
            throw new SecurityException("Error closing socket", e);
        }
    }
}