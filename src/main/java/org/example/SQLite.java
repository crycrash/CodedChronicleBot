package org.example;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLite {

    public Connection connection;

    public SQLite() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    public static void main(String[] args) {
    }

    public static void makeTable() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS reads ("
                    + "ip INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "id INT,"
                    + "message STRING,"
                    + "year STRING,"
                    + "month STRING,"
                    + "day STRING"
                    + ")";
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeNote(long chat_id, String text, String date) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        String insertSql = "INSERT INTO reads (id, message, year, month, day) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatementInsert = connection.prepareStatement(insertSql);
            preparedStatementInsert.setLong(1, chat_id);
            preparedStatementInsert.setString(2, text);
            preparedStatementInsert.setString(3, year);
            preparedStatementInsert.setString(4, month);
            preparedStatementInsert.setString(5, day);
            preparedStatementInsert.executeUpdate();

            String selectSql = "SELECT * FROM reads";
            PreparedStatement preparedStatementSelect = connection.prepareStatement(selectSql);
            ResultSet resultSet = preparedStatementSelect.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String message = resultSet.getString("message");
                int ip = resultSet.getInt("ip");
                String dat = resultSet.getString("year");
                String dat1 = resultSet.getString("month");
                String dat2 = resultSet.getString("day");
                System.out.println("ID: " + id + ", ip: " + ip + ", message: " + message + " Date: " + dat + " " + dat1 + " " + dat2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getMessage(long chat_id,String year, String month, String day) {
        String selectSql = "SELECT message FROM reads WHERE id = ? AND year = ? AND month = ? AND day = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setLong(1, chat_id);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, month);
            preparedStatement.setString(4, day);
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isResultSetNotEmpty = resultSet.next();
            if (isResultSetNotEmpty) {
                return resultSet.getString("message"); // вернуть найденное сообщение
            } else {
                return null; // если сообщение не найдено, вернуть null
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getYears(long chatId) {
        String selectSql = "SELECT year FROM reads WHERE id = ?";
        List<String> years = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setLong(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                years.add(resultSet.getString("year"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }
    public List<String> getMonths(long chatId, String year1) {
        String selectSql = "SELECT month FROM reads WHERE id = ? AND year = ?";
        List<String> months = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, year1);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                months.add(resultSet.getString("month"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return months;
    }
    public List<String> getDays(long chatId, String year1, String month1) {
        String selectSql = "SELECT day FROM reads WHERE id = ? AND year = ? AND month = ?";
        List<String> days = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, year1);
            preparedStatement.setString(3, month1);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                days.add(resultSet.getString("day"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return days;
    }
}