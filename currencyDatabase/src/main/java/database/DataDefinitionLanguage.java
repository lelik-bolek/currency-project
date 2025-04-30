package database;

import common.DatabaseManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.sql.*;

public class DataDefinitionLanguage {
    // Путь к ddl.sql относительно корня проекта
    private static final String SQL_FILE = "ddl.sql";

    public static void checkAndInitSchema() {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Начало транзакции

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet schemas = meta.getSchemas();
            while (schemas.next()) {    
                System.out.println("Доступные схемы: " + schemas.getString("TABLE_SCHEM"));
                }

            ResultSet tables = meta.getTables(null, "STAGING", null, new String[]{"TABLE"});
            

            boolean tablesExist = false;
            System.out.println("Проверка существования таблиц в схеме STAGING...");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Найдены таблицы: " + tableName);

                if (tableName.equalsIgnoreCase("ForeignCurrencyMarketLib") 
                    || tableName.equalsIgnoreCase("ForeignCurrencyMarket")) {
                    tablesExist = true;
                    break;
                }
            }

            if (!tablesExist) {
                System.out.println("Таблицы не найдены. Инициализация...");
                
                // 1. Получение содержимого SQL-файла
                String sqlContent;
				try (InputStream inputStream = DataDefinitionLanguage.class.getClassLoader().getResourceAsStream(SQL_FILE)) {
					if (inputStream == null) {
						throw new RuntimeException("Файл " + SQL_FILE + " не найден в classpath!");
					}
					sqlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					System.out.println("Файл ddl.sql успешно прочитан");
				} catch (IOException e) {
					throw new RuntimeException("Ошибка чтения ddl.sql", e);
				}

                // 2. Разделение SQL-запросов
                String[] sqlQueries;
                try {
                    sqlQueries = sqlContent.split(";\\s*\n");
                } catch (Exception e) {
                    throw new RuntimeException("Ошибка при разборе SQL-запросов", e);
                }

                // 3. Выполнение SQL-запросов
                try (Statement stmt = conn.createStatement()) {
                    for (String query : sqlQueries) {
                        if (query.trim().isEmpty()) continue;
                        
                        try {
                            stmt.execute(query);
                            System.out.println("Выполнен SQL-запрос: " + query.trim().split("\\s*\\(")[0] + "...");
                        } catch (SQLException e) {
                            throw new RuntimeException("Ошибка выполнения SQL-запроса: " + 
                                query.trim().split("\\s*\\(")[0] + "...", e);
                        }
                    }
                    conn.commit(); // Фиксация транзакции
                    System.out.println("Таблицы успешно созданы.");
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("Ошибка при создании Statement", e);
                }
            } else {
                System.out.println("Таблицы уже существуют.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при работе с БД: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}