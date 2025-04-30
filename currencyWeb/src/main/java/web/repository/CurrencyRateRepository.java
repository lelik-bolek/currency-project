package web.repository;

import web.dto.CurrencyRateDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
//import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Класс делает HTTP-запросы к currencyApi и получает список курсов в формате JSON.
 */
public class CurrencyRateRepository {
    //private static final ObjectMapper mapper = new ObjectMapper(); // JSON-парсер

    // Получить курс валюты на дату
    public List<CurrencyRateDTO> getRate(String charCode) throws Exception {
        URI uri = new URI("http", "localhost:8080", "/api/v1/currencies", "charCode=" + charCode, null);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

    // Проверка кода состояния ответа
        int statusCode = conn.getResponseCode();
        System.out.println("Ответ сервера (status code): " + statusCode);

    // Если код ответа не OK, выбрасываем исключение
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Ошибка при получении данных: " + statusCode);
        }        

        try (InputStream is = conn.getInputStream()) {
        // Читаем поток как строку
        String jsonResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        //System.out.println("Полученный JSON: " + jsonResponse);

        // Преобразуем строку обратно в InputStream
        InputStream jsonInputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));

        // Создаем ObjectMapper для работы с JSON
        ObjectMapper mapper = new ObjectMapper();

        // Создаем JsonParser для чтения потока
        JsonParser parser = mapper.getFactory().createParser(jsonInputStream);

        // Логируем весь JSON до обработки
        JsonNode rootNode = mapper.readTree(parser);
        System.out.println("JSON после обработки: " + rootNode.toString());

        // Теперь десериализуем данные в нужный формат
            return mapper.readValue(jsonResponse, new TypeReference<List<CurrencyRateDTO>>() {});
        }
    }

    // Получить курс за период
    public List<CurrencyRateDTO> getRateHistory(String charCode, String from, String to) throws Exception {
        String query = String.format("charCode=%s&fromDate=%s&toDate=%s", charCode, from, to);
        URI uri = new URI("http", "localhost:8080", "/api/v1/currencies/history", query, null);
        URL url = uri.toURL();
        // Печатаем итоговый URL для проверки
        System.out.println("==> Запрос уходит на: " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

            // Проверка кода состояния ответа
        int statusCode = conn.getResponseCode();
        System.out.println("Ответ сервера (status code): " + statusCode);

        // Если код ответа не OK, выбрасываем исключение
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Ошибка при получении данных: " + statusCode);
        }

        try (InputStream is = conn.getInputStream()) {
        // Читаем поток как строку
        String jsonResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        //System.out.println("Полученный JSON: " + jsonResponse);

        // Преобразуем строку обратно в InputStream
        InputStream jsonInputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));

        // Создаем ObjectMapper для работы с JSON
        ObjectMapper mapper = new ObjectMapper();

        // Создаем JsonParser для чтения потока
        JsonParser parser = mapper.getFactory().createParser(jsonInputStream);

        // Логируем весь JSON до обработки
        JsonNode rootNode = mapper.readTree(parser);
        System.out.println("JSON после обработки: " + rootNode.toString());

        // Теперь десериализуем данные в нужный формат
            return mapper.readValue(jsonResponse, new TypeReference<List<CurrencyRateDTO>>() {});
        }
    }
}
