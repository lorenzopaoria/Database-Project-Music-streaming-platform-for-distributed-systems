package com.example;

import com.example.dao.UserDAO;
import com.example.factory.DatabaseFactory;
import com.example.logging.DatabaseAuditLogger;
import com.example.security.RoleBasedAccessControl;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseServer {    
    private static final int PORT = 12345;
    private static final Logger LOGGER = Logger.getLogger(DatabaseServer.class.getName());
    private static final RoleBasedAccessControl ACCESS_CONTROL = new RoleBasedAccessControl();

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("Server listening on port " + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    LOGGER.info("Connection received from client");

                    threadPool.submit(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        } finally {
            threadPool.shutdown();
        }
    }

    static class ClientHandler implements Runnable {
        private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
        private final Socket clientSocket;
        private Connection connection;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private UserDAO userDAO;
        private String userId;
        private String userEmail;
        private String userRole;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.userId = generateRandomUserId();
        }

        @Override
        public void run() {
            try {
                setupStreams();
                setupDatabaseConnection();
                handleAuthentication();
                handleQueries();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in client handler", e);
            } finally {
                cleanup();
            }
        }

        private void setupStreams() throws IOException {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
        }

        private void setupDatabaseConnection() throws SQLException {
            connection = DatabaseFactory.getConnection();
            userDAO = new UserDAO(connection);
        }

        private void handleAuthentication() throws IOException, ClassNotFoundException {
            userEmail = (String) input.readObject();
            String password = (String) input.readObject();

            userRole = userDAO.authenticate(userEmail, password);

            if (userRole == null) {
                output.writeObject("Authentication failed!");
                output.flush();
                throw new SecurityException("Authentication failed");
            }

            String welcomeMessage = String.format("Authentication successful! Welcome, %s (User ID: %s)", userRole, userId);
            output.writeObject(welcomeMessage);
            output.flush();

            LOGGER.info(String.format("User %s (%s) authenticated with role %s", userEmail, userId, userRole));
        }

        private void handleQueries() throws IOException, ClassNotFoundException {
            while (!Thread.currentThread().isInterrupted()) {
                String query = (String) input.readObject();
                LOGGER.info(String.format("User %s (%s) sent query: %s", userEmail, userId, query));

                if ("exit".equalsIgnoreCase(query)) {
                    output.writeObject("Exiting...");
                    output.flush();
                    break;
                }

                try {
                    if (ACCESS_CONTROL.isAllowed(userRole, query)) {
                        String result = userDAO.executeQuery(query);
                        output.writeObject(result);
                    } else {
                        output.writeObject("Access denied: insufficient permissions.");
                    }
                    output.flush();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error executing query", e);
                    output.writeObject("Query execution error: " + e.getMessage());
                    output.flush();
                }
            }
        }

        private void cleanup() {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during cleanup", e);
            }
        }

        private String generateRandomUserId() {
            return UUID.randomUUID().toString().substring(0, 8);
        }
    }
}