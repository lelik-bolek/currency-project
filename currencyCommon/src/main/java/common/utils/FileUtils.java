package common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * Утилиты для работы с файловой системой.
 */
public class FileUtils {


/**
 * Рекурсивно удаляет все файлы внутри директории tmpfiles и её поддиректорий.
 * Сами директории остаются нетронутыми.
 * 
 * @throws IOException если произошла ошибка ввода-вывода при удалении файлов
 */
public static void removeFiles() throws IOException {
    Path tmpDir = Paths.get("resources", "tmpfiles");
    
    if (Files.exists(tmpDir)) {
        Files.walkFileTree(tmpDir, new SimpleFileVisitor<Path>() {
            /**
             * Обрабатывает каждый файл в директории
             * @param file текущий файл для удаления
             * @param attrs атрибуты файла
             * @return CONTINUE - продолжить обход
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    // Удаляем только файлы (не директории)
                    if (Files.isRegularFile(file)) {
                        Files.delete(file);
                        System.out.println("Удален файл: " + file);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при удалении файла " + file + ": " + e.getMessage());
                    throw e; // Пробрасываем исключение дальше
                }
                return FileVisitResult.CONTINUE;
            }

            /**
             * Обработка ошибок доступа к файлу
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("Невозможно обработать файл: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
    } else {
        System.out.println("Директория tmpfiles не существует: " + tmpDir);
        }
    }

    
    /**
     * Сохраняет XML-данные с указанного URL в файл по заданному пути.
     * Автоматически создает все необходимые директории.
     * 
     * @param url адрес для загрузки XML-данных
     * @param filePath относительный путь к файлу (от корня проекта)
     * @throws IOException если произошла ошибка при загрузке или сохранении файла
     * @throws URISyntaxException если URL имеет неверный формат
     * @throws IllegalArgumentException если URL или filePath являются null
     */
    public static void downloadXmlToFile(String url, String filePath) 
            throws IOException, URISyntaxException {
        System.out.println("Начало выполнения downloadXmlToFile()");

        // Проверка входных параметров
        if (url == null || filePath == null || url.isBlank() || filePath.isBlank()) {
            throw new IllegalArgumentException("URL и filePath не могут быть null или пустыми");
        }
    
        // Получаем путь в рамках проекта
        //Path fullPath = Paths.get("currency-project", "resources", filePath).toAbsolutePath(); // использует абсалют путь
        Path fullPath = Paths.get("resources", filePath).toAbsolutePath();
        System.out.println("Целевой путь: " + fullPath);

        // Создаем все необходимые директории
        Files.createDirectories(fullPath.getParent());
            
        // Загрузка и сохранение XML с использованием URI вместо URL
        URI uri = new URI(url);
        URLConnection conn = uri.toURL().openConnection();
        conn.setConnectTimeout(5000);  // Установка таймаутов
        conn.setReadTimeout(10000); // Время на чтение данных

        // Проверка HTTP-ответа перед загрузкой
        if (conn instanceof HttpURLConnection httpConn) {
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpConn.disconnect(); // Явное закрытие соединения при ошибке
                throw new IOException("Сервер вернул код ошибки: " + responseCode);
            }
        }

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, fullPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        System.out.println("Данные загружены");
    }

}

