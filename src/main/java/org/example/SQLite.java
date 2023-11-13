package org.example;
import java.sql.*;
import java.time.LocalDate;

public class SQLite {

    public Connection connection;
    public Statement statement;

    public SQLite() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    public static void main(String[] args) {
    }

    public static void makeTable() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS messages ("
                    + "ip INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "id INT,"
                    + "message STRING,"
                    + "date STRING"
                    + ")";
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeNote(long chat_id, String text, String date) {
        System.out.println(1);
        String insertSql = "INSERT INTO messages (id, message, date) VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatementInsert = connection.prepareStatement(insertSql);
            preparedStatementInsert.setLong(1, chat_id);
            preparedStatementInsert.setString(2, text);
            preparedStatementInsert.setString(3, date);
            preparedStatementInsert.executeUpdate();

            String selectSql = "SELECT * FROM messages";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(selectSql);
            ResultSet resultSet = preparedStatementSelect.executeQuery();

            while (resultSet.next()) {
                System.out.println(2);
                int id = resultSet.getInt("id");
                String message = resultSet.getString("message");
                int ip = resultSet.getInt("ip");
                String dat = resultSet.getString("date");
                System.out.println("ID: " + id + ", ip: " + ip + ", message: " + message + "Date: " + dat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getMessage(long chat_id, String date) {
        String selectSql = "SELECT message FROM mes WHERE id = ? AND date = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setLong(1, chat_id);
            preparedStatement.setString(2, date);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);  // вернуть найденное сообщение
            } else {
                return null;  // если сообщение не найдено, вернуть null
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}