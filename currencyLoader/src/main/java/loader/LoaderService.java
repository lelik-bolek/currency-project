package loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
//import java.sql.Connection;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
//import common.model.*;
//import common.utils.*;
import common.utils.DbUtils;
import common.utils.FileParsing;
import common.utils.FileUtils;


/**
 * Основной сервис для загрузки и обновления данных валют с сайта cbr.ru.
 */
public class LoaderService {

    /**
     * Обновляет данные в таблице ForeignCurrencyMarketLib.
     * Скачивает XML, удаляет старые записи и вставляет новые.
     */
    public static void updateTablesFCML() {
        final String tableName = "FOREIGNCURRENCYMARKETLIB"; //STAGING.
        final String urlString = "https://cbr.ru/scripts/XML_val.asp?d=0";
        final String filePath = "tmpfiles/lib/lib.xml";

        // → truncateTable("ForeignCurrencyMarketLib")
        try{
            DbUtils.truncateTable( tableName );
            } catch (SQLException e) {
            System.err.println("Ошибка при очистке таблицы: " + e.getMessage());
        }
        

        // → загрузка XML по https://cbr.ru/scripts/XML_val.asp?d=0
        try {
            FileUtils.downloadXmlToFile(urlString, filePath);
        } catch (URISyntaxException e) {
            // Обработка ошибок в URL
            System.err.println("Ошибка в формате URL '" + urlString + "': " + e.getMessage());
        } catch (ConnectException e) {
            // Ошибка соединения (таймаут или недоступность сервера)
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
        } catch (FileNotFoundException e) {
            // Файл не найден (HTTP 404)
            System.err.println("Файл не найден по указанному URL: " + urlString);
        } catch (SocketTimeoutException e) {
            // Таймаут при чтении данных
            System.err.println("Превышено время ожидания данных: " + e.getMessage());
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            } else if (e instanceof SocketTimeoutException) {
                System.err.println("Превышено время ожидания данных: " + e.getMessage());
            } else {
                System.err.println("Ошибка ввода-вывода при загрузке файла: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // → парсинг и вставка в таблицу
        try{
            // → парсинг
            List<Object> fcmlParsing= FileParsing.fileParsing( filePath );
            // → вставка в таблицу
            DbUtils.loadTable( fcmlParsing, tableName);
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println("Ошибка при разборе XML: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Ошибка при вставке в таблицу: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка: " + e.getMessage());
        }
    }

    
    /**
     * Обновляет данные в таблице ForeignCurrencyMarket за каждый день.
     * @param date дата начала загрузки (максимальная дата в БД).
     */
    public static void updateTablesFCM() {
        // Имя таблицы в БД (полное имя с схемой STAGING)
        final String tableName = "FOREIGNCURRENCYMARKET"; //STAGING.
        
        // Базовый URL для загрузки данных (без даты)
        final String baseUrl = "https://cbr.ru/scripts/XML_daily.asp?date_req=";
        
        // Базовый путь для сохранения файлов (директория)
        final String baseFilePath = "tmpfiles/date";
        
        // Получаем максимальную дату из таблицы, чтобы загружать только новые данные
        LocalDate maxDateInTable = DbUtils.checkingTablesFCM();
        System.out.println("Данные загружены на: " + maxDateInTable);
    
    
        // Текущая дата - до неё будем загружать
        final LocalDate today = LocalDate.now();
        System.out.println("Текущая дата: " + today);
        
        // Проверяем, нужно ли вообще что-то загружать
        if (!maxDateInTable.isBefore(today)) { // Если maxDate >= сегодня
            System.out.println("Данные уже актуальны, загрузка не требуется. Завершение работы метода updateTablesFCM().");
            return;
        }
        
        // Форматтер для даты в URL (ЦБ РФ требует dd/MM/yyyy)
        final DateTimeFormatter urlDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Форматтер для имени файла (используем дефисы вместо слэшей)
        final DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    
        // Начинаем обработку с дня, следующего за максимальным в таблице
        LocalDate currentDate = maxDateInTable.plusDays(1);
    
    
        // Цикл по дням от currentDate до сегодняшнего дня
        while (!currentDate.isAfter(today)) {
            // Форматируем дату для URL и имени файла
            String formattedDateForUrl = currentDate.format(urlDateFormatter);
            String formattedDateForFile = currentDate.format(fileNameFormatter);
    
            // Формируем полный URL и путь к файлу
            String url = baseUrl + formattedDateForUrl;
            String filePath = baseFilePath + "/" + formattedDateForFile + ".xml";
    
            try {
                System.out.println("Обработка данных за дату: " + formattedDateForUrl);
                
                // 1. Загрузка XML с ЦБ и сохранение в файл
                System.out.println("Загрузка файла...");
                FileUtils.downloadXmlToFile(url, filePath);
                
                // 2. Парсинг файла в DTO объекты
                System.out.println("Парсинг файла...");
                List<Object> parsedData = FileParsing.fileParsing(filePath);
                
                // 3. Если данные получены - загружаем в БД
                if (parsedData != null && !parsedData.isEmpty()) {
                    System.out.println("Загрузка в БД...");
                    DbUtils.loadTable(parsedData, tableName);
                    System.out.println("Успешно загружено " + parsedData.size() + " записей за " + formattedDateForUrl);
                } else {
                    System.out.println("Нет данных для загрузки за " + formattedDateForUrl);
                }
    
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке/сохранении файла за " + formattedDateForUrl + ": " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Ошибка при работе с БД за " + formattedDateForUrl + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Неожиданная ошибка при обработке даты " + formattedDateForUrl + ": " + e.getMessage());
                e.printStackTrace();
            }
    
            // Переход на следующий день
            currentDate = currentDate.plusDays(1);
        }
        
        System.out.println("Обновление таблицы " + tableName + " завершено");
    }
    
}

