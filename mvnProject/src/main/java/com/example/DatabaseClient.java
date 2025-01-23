package com.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DatabaseClient {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    // Test user credentials
    private static final List<UserTest> TEST_USERS = new ArrayList<>();
    static {
        TEST_USERS.add(new UserTest("margheritaursino@gmail.com", "marghe02", "free"));
        TEST_USERS.add(new UserTest("annapistorio@gmail.com", "anna04", "premium"));
    }

    // Test queries for different roles
    private static final List<QueryTest> QUERIES = new ArrayList<>();
    static {
        QUERIES.add(new QueryTest("SELECT * FROM Contenuto LIMIT 5", true));  // Select query (allowed for all)
        QUERIES.add(new QueryTest("UPDATE Contenuto SET nome = 'Test Title' WHERE idContenuto = 1", true));  // Update query (allowed for premium)
        QUERIES.add(new QueryTest("INSERT INTO Contenuto (nome, duarata, riproduzione, tipo) VALUES ('New Song', 180, 0, 1)", true));  // Insert query (allowed for premium)
        QUERIES.add(new QueryTest("DELETE FROM Contenuto WHERE idContenuto = 1", false));  // Delete query (restricted only for admin)
    }

    public static void main(String[] args) {
        // Run automated tests for each user
        TEST_USERS.forEach(DatabaseClient::runUserScenarioTest);
    }

    private static void runUserScenarioTest(UserTest user) {
        System.out.println("\n--- Testing User: " + user.email + " (Role: " + user.role + ") ---");
        
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            
            // Authenticate
            String sessionId = authenticate(output, input, user);
            if (sessionId == null) {
                System.out.println("Authentication failed for " + user.email);
                return;
            }

            // Test various queries
            QUERIES.forEach(query -> testQuery(output, input, sessionId, query, user));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String authenticate(ObjectOutputStream output, ObjectInputStream input, UserTest user) throws IOException, ClassNotFoundException {
        output.writeObject("AUTH");
        output.writeObject(user.email);
        output.writeObject(user.password);
        output.flush();
        
        // Receive and process authentication response
        String authResponse = (String) input.readObject();
        System.out.println("Authentication Result: " + authResponse);
        
        if (authResponse.startsWith("Authentication successful")) {
            return authResponse.split(":")[1].trim();
        }
        return null;
    }

    private static void testQuery(ObjectOutputStream output, ObjectInputStream input, String sessionId, QueryTest query, UserTest user) {
        try {
            // Send query request
            output.writeObject("QUERY");
            output.writeObject(sessionId);
            output.writeObject(query.sql);
            output.flush();
            
            // Receive query result
            String queryResult = (String) input.readObject();
            
            // Validate query result based on expected permission
            boolean isAllowed;
            if (user.role.equals("admin")) {
                // Admin can execute all queries
                isAllowed = true;
            } else if (user.role.equals("premium")) {
                // Premium users can execute SELECT, UPDATE, and INSERT queries
                isAllowed = query.expectedAllowed || query.sql.trim().toUpperCase().startsWith("SELECT");
            } else {
                // Free users can only execute SELECT queries
                isAllowed = query.sql.trim().toUpperCase().startsWith("SELECT");
            }
            
            boolean actuallyAllowed = !queryResult.contains("Access denied");
            
            System.out.println("\nQuery Test:");
            System.out.println("SQL: " + query.sql);
            System.out.println("Expected Allowed: " + query.expectedAllowed);
            System.out.println("User Role: " + user.role);
            System.out.println("Query Result: " + queryResult);
            System.out.println("Permission Check: " + 
                (isAllowed == actuallyAllowed ? "PASS" : "FAIL"));
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Inner classes to structure test data
    private static class UserTest {
        String email;
        String password;
        String role;

        UserTest(String email, String password, String role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }

    private static class QueryTest {
        String sql;
        boolean expectedAllowed;

        QueryTest(String sql, boolean expectedAllowed) {
            this.sql = sql;
            this.expectedAllowed = expectedAllowed;
        }
    }
}