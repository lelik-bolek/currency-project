package common;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Класс для управления подключением к базе данных.
 * Предоставляет статический метод для установления соединения с БД.
 * Параметры подключения (URL, пользователь, пароль) загружаются из файла config.properties.
 * 
 * 
 * @see Connection
 * @see DriverManager
 */
public class DatabaseManager {
    // Объявляем переменные для хранения параметров подключения
    private static final String url;
    private static final String user;
    private static final String password;

    static{
        try{ 
            // Получаем данные из config.properties
            InputStream input = DatabaseManager.class
                                .getClassLoader()
                                .getResourceAsStream("config.properties");

                if(input == null) {
                    throw new RuntimeException("Не удалось найти config.properties");
                }

            // Создаем объект Properties и загружаем в него данные из файла
            Properties prop = new Properties();
            prop.load(input);

            // Получаем значения параметров из properties
            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");
            
            // Закрываем поток
            input.close();
            
        } catch (Exception ex) {
            // В случае ошибки выводим stack trace и завершаем работу
            ex.printStackTrace();
            throw new RuntimeException("Ошибка загрузки конфигурации БД", ex);
        }


    }
    
    /**
     * Устанавливает и возвращает соединение с базой данных.
     * Использует параметры подключения, загруженные из конфигурационного файла.
     * 
     * @return Объект Connection, представляющий соединение с БД
     * @throws SQLException если произошла ошибка при установке соединения
     * @see DriverManager#getConnection(String, String, String)
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Явная регистрация JDBC-драйвера (важно для запуска через exec-maven-plugin)
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 JDBC Driver не найден.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(url, user, password);
    }
}
