package com.example;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class QueryGUI {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(QueryGUI::createLoginFrame);
    }

    private static void createLoginFrame() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 350);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setUndecorated(true);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loginPanel.setBackground(new Color(240, 248, 255)); // Light blue background

        JLabel titleLabel = new JLabel("Database Query Tool", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 123, 255)); // Azzurro
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField emailField = new JTextField(15);
        emailField.setMaximumSize(new Dimension(300, 40));
        emailField.setBorder(BorderFactory.createTitledBorder("Email"));

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 123, 255)); // Azzurro
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(titleLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(emailField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        loginPanel.add(loginButton);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(statusLabel);

        loginFrame.add(loginPanel);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);

        loginButton.addMouseListener(new MouseAdapter() {
            Timer hoverTimer;
            int alpha = 255;

            @Override
            public void mouseEntered(MouseEvent e) {
                hoverTimer = new Timer(10, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        alpha = Math.max(200, alpha - 5);
                        loginButton.setBackground(new Color(0, 123, 255, alpha));
                        if (alpha == 200) hoverTimer.stop();
                    }
                });
                hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverTimer = new Timer(10, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        alpha = Math.min(255, alpha + 5);
                        loginButton.setBackground(new Color(0, 123, 255, alpha));
                        if (alpha == 255) hoverTimer.stop();
                    }
                });
                hoverTimer.start();
            }
        });

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
                        statusLabel.setText("Login successful!");

                        JProgressBar progressBar = new JProgressBar();
                        progressBar.setIndeterminate(true);
                        progressBar.setForeground(new Color(0, 123, 255));
                        progressBar.setBackground(Color.WHITE);
                        loginPanel.add(progressBar);
                        loginPanel.revalidate();
                        loginPanel.repaint();

                        Timer timer = new Timer(1500, actionEvent -> {
                            loginFrame.dispose();
                            showQueryGUI(socket, output, input);
                        });
                        timer.setRepeats(false);
                        timer.start();
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
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255)); // Light blue background

        JTextField queryField = new JTextField();
        queryField.setBorder(BorderFactory.createTitledBorder("Enter SQL Query"));
        queryField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton sendButton = new JButton("Send Query");
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        resultArea.setBorder(BorderFactory.createTitledBorder("Query Results"));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        ((DefaultCaret) resultArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JLabel statusLabel = new JLabel("Connected", SwingConstants.LEFT);
        statusLabel.setForeground(new Color(0, 128, 0));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(queryField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText();
                if (query.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a query.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    output.writeObject(query);
                    output.flush();

                    if ("exit".equalsIgnoreCase(query)) {
                        statusLabel.setText("Disconnected");
                        socket.close();
                        frame.dispose();
                        return;
                    }

                    String result = (String) input.readObject();
                    resultArea.append("Query: " + query + "\n");
                    resultArea.append("Result:\n" + result + "\n\n");

                    Timer scrollTimer = new Timer(15, new ActionListener() {
                        int scrollPos = scrollPane.getVerticalScrollBar().getValue();

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            scrollPane.getVerticalScrollBar().setValue(scrollPos += 10);
                            if (scrollPos >= scrollPane.getVerticalScrollBar().getMaximum()) {
                                ((Timer) e.getSource()).stop();
                            }
                        }
                    });
                    scrollTimer.start();
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(frame, "Error sending query: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
