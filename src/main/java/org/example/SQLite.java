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
    public boolean check(long chat_id, String year, String month, String day) {
        // Проверяем, есть ли уже такая запись
        String checkSql = "SELECT COUNT(*) FROM reads WHERE id = ? AND year = ? AND month = ? AND day = ?";

        try (PreparedStatement preparedStatementCheck = connection.prepareStatement(checkSql)) {
            preparedStatementCheck.setLong(1, chat_id);
            preparedStatementCheck.setString(2, year);
            preparedStatementCheck.setString(3, month);
            preparedStatementCheck.setString(4, day);

            try (ResultSet resultSetCheck = preparedStatementCheck.executeQuery()) {
                if (resultSetCheck.next()) {
                    int count = resultSetCheck.getInt(1);
                    return count>0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Здесь можно добавить логирование или дополнительную обработку
        }

        // Если метод не вернул значение раньше, значит произошла ошибка
        return false;
    }
    public void makeNote(long chat_id, String text, String year, String month, String day) {

        String insertSql = "INSERT INTO reads (id, message, year, month, day) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertSql)) {
            preparedStatementInsert.setLong(1, chat_id);
            preparedStatementInsert.setString(2, text);
            preparedStatementInsert.setString(3, year);
            preparedStatementInsert.setString(4, month);
            preparedStatementInsert.setString(5, day);
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void add (long chat_id, String text, String year, String month, String day) {
        String insertSql = "UPDATE reads (id, message, year, month, day) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertSql)) {
            preparedStatementInsert.setLong(1, chat_id);
            preparedStatementInsert.setString(2, text);
            preparedStatementInsert.setString(3, year);
            preparedStatementInsert.setString(4, month);
            preparedStatementInsert.setString(5, day);
            preparedStatementInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Integer> makeNoteNo(long chat_id, String year, String month){
        Calendar cal = new Calendar();
        String selectSql = "SELECT day, month FROM reads WHERE id = ? AND year = ? AND month = ?";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(selectSql)){
            preparedStatement.setLong(1, chat_id);
            preparedStatement.setString(2, year);
            preparedStatement.setString(3, month);
            ResultSet resultSet = preparedStatement.executeQuery();
            String d;
            while(resultSet.next()){
                d = resultSet.getString("day");
                System.out.println(cal.calendar.get(Integer.parseInt(month) - 1));
                System.out.println(d);
                cal.calendar.get(Integer.parseInt(month) - 1).set(Integer.parseInt(d) - 1, 0);
            }
            return cal.calendar.get(Integer.parseInt(month) - 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getMessage(long chat_id,String year, String month, String day) {
        String selectSql = "SELECT message FROM reads WHERE id = ? AND year = ? AND month = ? AND day = ?";
        try
                (PreparedStatement preparedStatement = connection.prepareStatement(selectSql)){
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
    public void rewrite(long chat_id, String text, String year, String month, String day) {
        String sql = "UPDATE reads SET message = ? WHERE id = ? AND year = ? AND month = ? AND day = ?";

        try
            (PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, text);
            preparedStatement.setLong(2, chat_id);
            preparedStatement.setString(3, year);
            preparedStatement.setString(4, month);
            preparedStatement.setString(5, day);

            // Выполняем запрос
            int rowsAffected = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public List<String> getYears(long chatId) {
        String selectSql = "SELECT DISTINCT year FROM reads WHERE id = ?";
        List<String> years = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSql)) {
            preparedStatement.setLong(1, chatId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    years.add(resultSet.getString("year"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }
    public List<String> getMonths(long chatId, String year1) {
        String selectSql = "SELECT DISTINCT month FROM reads WHERE id = ? AND year = ?";
        List<String> months = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSql)) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, year1);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    months.add(resultSet.getString("month"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return months;
    }
    public List<String> getDays(long chatId, String year1, String month1) {
        String selectSql = "SELECT DISTINCT day FROM reads WHERE id = ? AND year = ? AND month = ?";
        List<String> days = new ArrayList<>();
        // Используем try-with-resources для автоматического закрытия ресурсов
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSql)) {
            preparedStatement.setLong(1, chatId);
            preparedStatement.setString(2, year1);
            preparedStatement.setString(3, month1);
            try (ResultSet resultSet = preparedStatement.executeQuery()) { // и для ResultSet тоже
                while (resultSet.next()) {
                    days.add(resultSet.getString("day"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // В реальных приложениях лучше использовать логгер
        }
        return days;
    }
}