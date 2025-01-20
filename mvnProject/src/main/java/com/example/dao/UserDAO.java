package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public String authenticate(String email, String password) throws SQLException {
        String query = "SELECT tu.tipo \n" + //
                        "        FROM Utente u \n" + //
                        "        JOIN Tipo_Utente tu ON u.tipo = tu.idTipoUtente \n" + //
                        "        WHERE u.email = ? AND u.passw = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String tipoUtente = rs.getString("tipo");
                return tipoUtente.toLowerCase();
            }
            return null;
        }
    }

    public String executeQuery(String query) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            boolean isSelect = query.trim().toUpperCase().startsWith("SELECT");
            if (isSelect) {
                ResultSet rs = stmt.executeQuery();
                return formatResultSet(rs);
            } else {
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected + " rows affected";
            }
        }
    }

    private String formatResultSet(ResultSet rs) throws SQLException {
        StringBuilder result = new StringBuilder();
        int columnCount = rs.getMetaData().getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            result.append(rs.getMetaData().getColumnName(i)).append("\t");
        }
        result.append("\n");

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                result.append(rs.getString(i)).append("\t");
            }
            result.append("\n");
        }
        return result.toString();
    }
}