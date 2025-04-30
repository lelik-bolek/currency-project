package api;

import api.controller.CurrencyMarketController;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

/**
 * Точка входа для запуска API-сервера.
 * Этот класс создает и запускает простой HTTP-сервер на порту 8080.
 */
public class ApiApp {
    public static void main(String[] args) throws Exception {
        // Создание HTTP-сервера на порту 8080
        // InetSocketAddress - это комбинация IP-адреса и номера порта
        // 0 в качестве второго параметра - это размер очереди входящих соединений (используется значение по умолчанию)
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Регистрация пути и контроллера
        // Здесь мы говорим серверу: "Когда кто-то обращается по адресу /api/v1/currencies,
        // используй для обработки запроса наш CurrencyMarketController"
        server.createContext("/api/v1/currencies", new CurrencyMarketController());

        // Запуск сервера
        // После этого вызова сервер начинает "слушать" порт 8080 и обрабатывать входящие запросы
        server.start();

        // Вывод сообщения в консоль, чтобы знать, что сервер успешно запустился
        System.out.println("API сервер запущен на http://localhost:8080/");
    }
}