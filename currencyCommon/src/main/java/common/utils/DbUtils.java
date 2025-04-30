package common.utils;

//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import common.DatabaseManager;
import common.model.ForeignCurrencyLibDto;
import common.model.ForeignCurrencyMarketDto;

/**
 * Утилиты для работы с таблицами базы данных.
 */
public class DbUtils {

    /**
    * Очищает (DELETE FROM) указанную таблицу в базе данных.
    * 
    * @param tableName Имя таблицы для очистки. Должно соответствовать имени существующей таблицы.
    * @throws SQLException Если произошла ошибка при выполнении SQL-запроса или таблица не найдена.
    */
    public static void truncateTable(String tableName) throws SQLException {
        System.out.println("Выполняет очистку таблицы: " + tableName);

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
                // 1. Проверяем существование таблицы
                checkingExistenceTable(tableName);
        
                // 2. Если проверка пройдена - выполняем DELETE FROM
                String truncatTable = String.format("DELETE FROM STAGING.%s", tableName);
                //String truncatTable = "DELETE FROM STAGING.FOREIGNCURRENCYMARKETLIB";
                stmt.executeUpdate(truncatTable);
                }
                
                System.out.printf("Таблица STAGING.%s успешно очищена.\n\n", tableName);
    }

    /**
    * Проверяет есть ли указанная таблица в базе данных.
    * 
    * @param tableName Имя таблицы. Должно соответствовать имени существующей таблицы.
    * @throws SQLException Если произошла ошибка или таблица не найдена.
    */
    public static void checkingExistenceTable(String tableName) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
                // Проверяем, существует ли таблица
                DatabaseMetaData meta = conn.getMetaData();
                try(ResultSet tables = meta.getTables(null, "STAGING", tableName, null)){
                    if(!tables.next()){
                        throw new SQLException("Таблица " + tableName + " не существует!");
                    }
                }
            }
    }

    /**
 * Метод для загрузки данных в таблицу базы данных.
 * Определяет тип DTO объектов в списке и выполняет соответствующую вставку данных,
 * учитывая особенности каждой таблицы (внешние ключи, типы данных, форматирование значений).
 * 
 * Поддерживает два типа данных:
 * 1. ForeignCurrencyLibDto - для таблицы библиотеки валют
 * 2. ForeignCurrencyMarketDto - для таблицы ежедневных курсов валют
 * 
 * Особенности обработки:
 * - Для ForeignCurrencyMarketDto автоматически конвертирует LocalDate в java.sql.Date
 * - Все числовые значения обрабатываются как целые числа (предполагается, что конвертация уже выполнена)
 * - Использует пакетную вставку для оптимизации производительности
 * - Выполняет операции в транзакции для обеспечения целостности данных
 * 
 * @param data Список объектов DTO для вставки (ForeignCurrencyLibDto или ForeignCurrencyMarketDto)
 * @param tableName Имя таблицы, в которую производится вставка
 * @throws SQLException при ошибках работы с базой данных
 * @throws IllegalArgumentException если передан пустой список или неподдерживаемый тип DTO
 */
public static void loadTable(List<Object> data, String tableName) throws SQLException {
    // Проверка входных данных
    if (data == null || data.isEmpty()) {
        throw new IllegalArgumentException("Нет данных для загрузки");
    }

    // Получаем соединение с базой данных
    try (Connection conn = DatabaseManager.getConnection()) {
        // Отключаем авто-коммит для выполнения в транзакции
        conn.setAutoCommit(false);

        // Обработка данных библиотеки валют (ForeignCurrencyLibDto)
        if (data.get(0) instanceof ForeignCurrencyLibDto) {
            // SQL-запрос для вставки в таблицу библиотеки валют
            String sql = "INSERT INTO STAGING." + tableName + " (id, Name, EngName, Nominal, ParentCode) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Пакетная обработка всех DTO объектов
                for (Object obj : data) {
                    ForeignCurrencyLibDto dto = (ForeignCurrencyLibDto) obj;
                    // Устанавливаем параметры запроса
                    stmt.setString(1, dto.getId());
                    stmt.setString(2, dto.getName());
                    stmt.setString(3, dto.getEngName());
                    stmt.setInt(4, dto.getNominal());
                    stmt.setInt(5, dto.getParentCode());
                    // Добавляем в пакет
                    stmt.addBatch();
                }
                // Выполняем пакетную вставку
                stmt.executeBatch();
            }

        // Обработка данных ежедневных курсов валют (ForeignCurrencyMarketDto)
        } else if (data.get(0) instanceof ForeignCurrencyMarketDto) {
            // SQL-запрос для вставки в таблицу курсов валют
            // "Экранирование "Value"
            String sql = "INSERT INTO STAGING." + tableName + " (ValuteID, NumCode, CharCode, \"Value\", VunitRate, DateReq) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Пакетная обработка всех DTO объектов
                for (Object obj : data) {
                    ForeignCurrencyMarketDto dto = (ForeignCurrencyMarketDto) obj;
                    // Устанавливаем параметры запроса
                    stmt.setString(1, dto.getValuteId());  // Внешний ключ на таблицу библиотеки валют
                    stmt.setInt(2, dto.getNumCode());
                    stmt.setString(3, dto.getCharCode());
                    stmt.setBigDecimal(4, dto.getValue());      // Уже сконвертированное значение (запятые удалены)
                    stmt.setBigDecimal(5, dto.getVunitRate());  // Уже сконвертированное значение (запятые удалены)
                    // Конвертируем LocalDate в java.sql.Date
                    stmt.setDate(6, Date.valueOf(dto.getDateReq()));
                    // Добавляем в пакет
                    stmt.addBatch();
                }
                // Выполняем пакетную вставку
                stmt.executeBatch();
            }

        // Обработка неподдерживаемых типов данных
        } else {
            throw new IllegalArgumentException("Неподдерживаемый тип данных: " + data.get(0).getClass().getName());
        }

        // Фиксируем транзакцию
        conn.commit();
    }

}

/**
 * Проверяет наличие данных в таблице ForeignCurrencyMarket.
 * @return максимальная дата (DateReq) или 01.01.2025, если данных нет.
 */
public static LocalDate checkingTablesFCM() {
    // Дата по умолчанию, которая будет возвращена, если таблица пуста
    LocalDate defaultDate = LocalDate.of(2025, 1, 1); // 01.01.2025
    LocalDate maxDate = defaultDate;
    
    // SQL-запрос для получения максимальной даты из таблицы
    String sql = "SELECT MAX(DateReq) FROM STAGING.FOREIGNCURRENCYMARKET";
    
    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        // Если результат содержит данные и первое поле не NULL
        if (rs.next()) {
            java.sql.Date sqlDate = rs.getDate(1);
            if (sqlDate != null) {
                // Преобразуем java.sql.Date в java.time.LocalDate
                maxDate = sqlDate.toLocalDate();
            }
        }
        
    } catch (SQLException e) {
        System.err.println("Ошибка при получении max(DateReq): " + e.getMessage());
        // В случае ошибки возвращаем дату по умолчанию
        return defaultDate;
    }
    
    return maxDate;
}


}





