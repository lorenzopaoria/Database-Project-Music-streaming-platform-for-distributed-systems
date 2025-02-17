package com.example;

import com.example.rbac.Role;
import com.example.rbac.Permission;
import com.example.session.Session;
import com.example.dao.UserDAO;
import com.example.factory.DatabaseFactory;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseServer {
    private static final int PORT = 12345;
    private static final Logger LOGGER = Logger.getLogger(DatabaseServer.class.getName());
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new HashMap<>();
    private final ExecutorService threadPool;
    private final UserDAO userDAO;
    private ServerSocket serverSocket;
    private boolean running;

    public DatabaseServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.userDAO = new UserDAO(DatabaseFactory.getConnection());
        initializeRoles();
    }

    public static void main(String[] args) {
        LOGGER.info("Starting Database Server...");
        DatabaseServer server = new DatabaseServer();
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            LOGGER.info("Server listening on port " + PORT);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    LOGGER.info("New client connected: " + clientSocket.getInetAddress());
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not listen on port " + PORT, e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            DatabaseFactory.getConnection().close();
            LOGGER.info("Audit logger closed.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during server shutdown", e);
            threadPool.shutdownNow();
        }
    }
    

    public Role getRole(String roleName) {
        return roles.get(roleName);
    }

    public void addSession(Session session) {
        sessions.put(session.getSessionId(), session);
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private void initializeRoles() {
        Role adminRole = new Role("admin");
        adminRole.addPermission(new Permission("SELECT", "*"));
        adminRole.addPermission(new Permission("INSERT", "*"));
        adminRole.addPermission(new Permission("UPDATE", "*"));
        adminRole.addPermission(new Permission("DELETE", "*"));
        adminRole.addPermission(new Permission("CREATE", "*"));
        adminRole.addPermission(new Permission("DROP", "*"));
        roles.put("admin", adminRole);

        Role premiumRole = new Role("premium");
        premiumRole.addPermission(new Permission("SELECT", "*"));
        premiumRole.addPermission(new Permission("INSERT", "*"));
        premiumRole.addPermission(new Permission("UPDATE", "*"));
        roles.put("premium", premiumRole);

        Role freeRole = new Role("free");
        freeRole.addPermission(new Permission("SELECT", "*"));
        roles.put("free", freeRole);
    }

    public class ClientHandler implements Runnable {
        private final Socket socket;
        private final DatabaseServer server;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        @SuppressWarnings("unused")
        private Session session;
        private String clientId;

        public ClientHandler(Socket socket, DatabaseServer server) {
            this.socket = socket;
            this.server = server;
            this.clientId = UUID.randomUUID().toString().substring(0, 8);
        }

        private String currentUserRole;
        private String result;

        public String getClientId() {
            return this.clientId;
        }

        public String getCurrentUserRole() {
            return this.currentUserRole;
        }

        public String getResult() {
            return this.result;
        }

        @Override
        public void run() {
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());

                while (true) {
                    String command = (String) input.readObject();
                    switch (command) {
                        case "AUTH":
                            handleAuthentication();
                            break;
                        case "QUERY":
                            handleQuery();
                            break;
                        case "EXIT":
                            return;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error handling client connection", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing client socket", e);
                }
            }
        }

        private void handleAuthentication() throws IOException, ClassNotFoundException {
            String email = (String) input.readObject();
            String password = (String) input.readObject();
        
            try {
                String userRole = userDAO.authenticate(email, password);
                if (userRole != null) {
                    this.currentUserRole = userRole;  // Imposta il ruolo dell'utente
                    Session newSession = new Session(email);
                    newSession.activate(server.getRole(userRole));
                    server.addSession(newSession);
                    session = newSession;
                    output.writeObject("Authentication successful:" + newSession.getSessionId());
                } else {
                    output.writeObject("Authentication failed");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Authentication error", e);
                output.writeObject("Authentication error: " + e.getMessage());
            }
            output.flush();
        }
        
        private void handleQuery() throws IOException, ClassNotFoundException {
            String sessionId = (String) input.readObject();
            String query = (String) input.readObject();
        
            Session session = server.getSession(sessionId);
            if (session == null || session.isExpired()) {
                result = "Session expired";  // Store result before sending
                output.writeObject(result);
                output.flush();
                return;
            }
        
            try {
                if (validateQueryPermissions(query, session.getActiveRoles())) {
                    result = userDAO.executeQuery(query);  // Store the query result
                    output.writeObject(result);
                } else {
                    result = "Access denied: Insufficient permissions";  // Store denied access message
                    output.writeObject(result);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Query execution error", e);
                result = "Query execution error: " + e.getMessage();
                output.writeObject(result);
            }
            output.flush();
        }
        

        private boolean validateQueryPermissions(String query, Set<Role> roles) {
            String operation = extractOperation(query);
            String object = extractObject(query);

            return roles.stream()
                .anyMatch(role -> role.hasPermission(new Permission(operation, object)) ||
                                role.hasPermission(new Permission(operation, "*")));
        }

        private String extractOperation(String query) {
            String upperQuery = query.trim().toUpperCase();
            return upperQuery.split("\\s+")[0];
        }

        private String extractObject(String query) {
            String upperQuery = query.trim().toUpperCase();
            String[] parts = upperQuery.split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("FROM") || parts[i].equals("INTO") || 
                    parts[i].equals("UPDATE")) {
                    return parts[i + 1];
                }
            }
            return "*";
        }
    }
}



