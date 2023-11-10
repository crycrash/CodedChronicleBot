package org.example;
import java.sql.*;

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
            //System.out.println("Таблица создана успешно!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeNote(long chat_id, String text) {
        System.out.println(1);
        String insertSql = "INSERT INTO messages (id, message) VALUES (?, ?)";
        try {
            PreparedStatement preparedStatementInsert = connection.prepareStatement(insertSql);
            preparedStatementInsert.setLong(1, chat_id);
            preparedStatementInsert.setString(2, text);
            preparedStatementInsert.executeUpdate();

            String selectSql = "SELECT * FROM messages";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(selectSql);
            ResultSet resultSet = preparedStatementSelect.executeQuery();

            while (resultSet.next()) {
                System.out.println(2);
                int id = resultSet.getInt("id");
                String message = resultSet.getString("message");
                int ip = resultSet.getInt("ip");
                System.out.println("ID: " + id + ", ip: " + ip + ", message: " + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}