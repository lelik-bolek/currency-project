package web;

import com.sun.net.httpserver.HttpServer;
import web.view.CurrencyRateHandler;

import java.net.InetSocketAddress;

/**
 * Главный класс запускает встроенный HTTP-сервер.
 * Сервер будет слушать порт 8081.
 */
public class WebApp {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        CurrencyRateHandler handler = new CurrencyRateHandler();

        // Один и тот же обработчик для трёх путей
        server.createContext("/", handler);
        server.createContext("/rate", handler);
        server.createContext("/rate-history", handler);

        server.setExecutor(null);
        server.start();

        System.out.println("Web-сервер запущен на http://localhost:8081/");
    }
}
