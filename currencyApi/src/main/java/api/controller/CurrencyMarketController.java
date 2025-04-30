package api.controller;

import api.dto.CurrencyRateResponse;
import common.DatabaseManager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REST-контроллер для обработки HTTP-запросов, связанных с курсами валют.
 * Реализует интерфейс HttpHandler для обработки входящих HTTP-запросов.
 * 
 * Основные функции:
 * - Обработка GET-запросов
 * - Извлечение параметров из query string
 * - Валидация обязательных параметров
 * - Запрос данных из базы данных
 * - Формирование JSON-ответа
 * - Обработка ошибок
 */
public class CurrencyMarketController implements HttpHandler {

    /**
     * Основной метод обработки HTTP-запросов.
     * 
     * @param exchange объект HttpExchange, содержащий информацию о HTTP-запросе и ответе
     */
    @Override
    public void handle(HttpExchange exchange) {
        try {
            // Проверяем метод запроса (должен быть GET)
            if (!"GET".equals(exchange.getRequestMethod())) {
                // Если метод не GET, возвращаем 405 (Method Not Allowed)
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            // Получаем URI запроса и параметры из query string
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();

            // Инициализация переменных для параметров запроса
            String charCode = null;    // Код валюты (например: USD, EUR)
            String fromDate = null;     // Начальная дата периода (формат: YYYY-MM-DD)
            String toDate = null;       // Конечная дата периода (формат: YYYY-MM-DD)

            // Разбираем query string, если она есть
            if (query != null) {
                // Разделяем параметры по символу &
                for (String param : query.split("&")) {
                    // Разделяем каждый параметр на ключ и значение
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        switch (kv[0]) {
                            case "charCode" -> charCode = kv[1].toUpperCase(); // Приводим код валюты к верхнему регистру
                            case "fromDate" -> fromDate = kv[1];
                            case "toDate" -> toDate = kv[1];
                        }
                    }
                }
            }

            // Проверяем обязательный параметр charCode
            if (charCode == null) {
                // Если код валюты не указан, возвращаем 400 (Bad Request)
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            // Получаем данные в зависимости от параметров запроса
            List<CurrencyRateResponse> result;
            if (fromDate != null && toDate != null) {
                // Если указаны обе даты - запрашиваем курс за период
                result = getCurrencyRateForPeriod(charCode, fromDate, toDate);
            } else {
                // Если даты не указаны - запрашиваем текущий курс
                result = getLatestCurrencyRate(charCode);
            }

            // Вручную формируем JSON-ответ из списка результатов
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < result.size(); i++) {
                CurrencyRateResponse resp = result.get(i);
                json.append(resp.toJson()); // Используем метод toJson() объекта CurrencyRateResponse
                if (i < result.size() - 1) json.append(","); // Добавляем запятую между элементами
            }
            json.append("]");

            // Подготавливаем ответ
            byte[] response = json.toString().getBytes();
            // Устанавливаем заголовок Content-Type
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            // Отправляем HTTP-заголовки с кодом 200 (OK) и длиной контента
            exchange.sendResponseHeaders(200, response.length);
            
            // Записываем тело ответа
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                // При любой ошибке возвращаем 500 (Internal Server Error)
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Получает последний доступный курс валюты по её коду.
     * 
     * @param charCode код валюты (например: USD, EUR)
     * @return список объектов CurrencyRateResponse (обычно содержит один элемент)
     * @throws SQLException при ошибках работы с базой данных
     */
    private List<CurrencyRateResponse> getLatestCurrencyRate(String charCode) throws SQLException {
        // SQL-запрос для получения последнего курса валюты
        String sql = """
            SELECT lib.Nominal, fcm.CharCode, fcm.VunitRate, fcm.DateReq
            FROM STAGING.ForeignCurrencyMarket fcm
            JOIN STAGING.ForeignCurrencyMarketLib lib ON fcm.ValuteID = lib.id
            WHERE fcm.CharCode = ?
              AND fcm.DateReq = (SELECT MAX(DateReq) FROM STAGING.ForeignCurrencyMarket)
            """;

        // Получаем соединение с базой данных и выполняем запрос
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Устанавливаем параметр запроса (код валюты)
            stmt.setString(1, charCode);
            ResultSet rs = stmt.executeQuery();

            // Преобразуем ResultSet в список объектов CurrencyRateResponse
            List<CurrencyRateResponse> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CurrencyRateResponse(
                        rs.getString("CharCode"),
                        rs.getDouble("VunitRate"),
                        rs.getInt("Nominal"),
                        rs.getDate("DateReq").toString()
                ));
            }
            return result;
        }
    }

    /**
     * Получает курс валюты за указанный период.
     * 
     * @param charCode код валюты (например: USD, EUR)
     * @param from начальная дата периода (формат: YYYY-MM-DD)
     * @param to конечная дата периода (формат: YYYY-MM-DD)
     * @return список объектов CurrencyRateResponse за указанный период
     * @throws SQLException при ошибках работы с базой данных
     */
    private List<CurrencyRateResponse> getCurrencyRateForPeriod(String charCode, String from, String to) throws SQLException {
        // SQL-запрос для получения курса валюты за период
        String sql = """
            SELECT lib.Nominal, fcm.CharCode, fcm.VunitRate, fcm.DateReq
            FROM STAGING.ForeignCurrencyMarket fcm
            JOIN STAGING.ForeignCurrencyMarketLib lib ON fcm.ValuteID = lib.id
            WHERE fcm.CharCode = ?
              AND fcm.DateReq BETWEEN ? AND ?
            ORDER BY fcm.DateReq DESC
            """;

        // Получаем соединение с базой данных и выполняем запрос
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Устанавливаем параметры запроса
            stmt.setString(1, charCode);
            stmt.setString(2, from);
            stmt.setString(3, to);
            ResultSet rs = stmt.executeQuery();

            // Преобразуем ResultSet в список объектов CurrencyRateResponse
            List<CurrencyRateResponse> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CurrencyRateResponse(
                        rs.getString("CharCode"),
                        rs.getDouble("VunitRate"),
                        rs.getInt("Nominal"),
                        rs.getDate("DateReq").toString()
                ));
            }
            return result;
        }
    }
}