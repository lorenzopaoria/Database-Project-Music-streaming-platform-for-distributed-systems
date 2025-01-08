package main.java.com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class QueryGUI {

    public static void main(String[] args) {

        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 300);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to Database Query Tool", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField emailField = new JTextField(15);
        emailField.setMaximumSize(new Dimension(200, 25));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setMaximumSize(new Dimension(200, 25));

        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginPanel.add(titleLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(emailLabel);
        loginPanel.add(emailField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(loginButton);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(statusLabel);

        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                try {
                    Socket socket = new Socket("127.0.0.1", 12345);
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    output.flush();

                    output.writeObject(email);
                    output.writeObject(password);
                    output.flush();

                    String response = (String) input.readObject();

                    if (response.startsWith("Authentication successful")) {
                        statusLabel.setForeground(Color.GREEN);
                        statusLabel.setText("Login successful! Loading query GUI...");

                        loginFrame.dispose();

                        showQueryGUI(socket, output, input);
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Login failed: " + response);
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        });
    }

    private static void showQueryGUI(Socket socket, ObjectOutputStream output, ObjectInputStream input) {

        JFrame frame = new JFrame("Database Query GUI");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel queryLabel = new JLabel("Enter SQL Query:");
        JTextField queryField = new JTextField(40);
        JButton sendButton = new JButton("Send Query");
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        JLabel statusLabel = new JLabel("Status: Connected to server", SwingConstants.LEFT);
        statusLabel.setForeground(new Color(0, 128, 0));

        JPanel inputPanel = new JPanel();
        inputPanel.add(queryLabel);
        inputPanel.add(queryField);
        inputPanel.add(sendButton);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                if (query.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please, enter a query: ", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    output.writeObject(query);
                    output.flush();

                    if ("exit".equalsIgnoreCase(query)) {
                        statusLabel.setText("Status: Disconnected");
                        socket.close();
                        frame.dispose();
                        return;
                    }

                    String result = (String) input.readObject();
                    resultArea.append("Query: " + query + "\n");
                    resultArea.append("Result: \n" + result + "\n\n");
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(frame, "Error sending query: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }
}
