package database;

import java.sql.*;
import common.DatabaseManager;

public class DatabaseApp {
    public static void main(String[] args) {
        try (Connection connection = DatabaseManager.getConnection()) {
            System.out.println(" Подключение к базе данных успешно");

            // метод, который проверяет и создаёт таблицы
            DataDefinitionLanguage.checkAndInitSchema();

            System.out.println(" Таблицы проверены/созданы");

        } catch (SQLException e) {
            System.err.println(" Ошибка при работе с БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}