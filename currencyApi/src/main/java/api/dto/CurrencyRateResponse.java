package api.dto;

import java.util.Locale;

/**
 * Data Transfer Object (DTO) класс, представляющий курс валюты.
 * Используется для передачи данных о курсе валюты между слоями приложения
 * и формирования JSON-ответа клиенту.
 * 
 * Содержит информацию:
 * - Код валюты (например: USD, EUR)
 * - Текущий курс
 * - Номинал (количество единиц валюты, к которому применяется курс)
 * - Дата актуальности курса
 */
public class CurrencyRateResponse {
    // Поля класса объявлены как final, что делает объект неизменяемым (immutable)
    
    /**
     * Код валюты в стандарте ISO 4217 (3 буквы)
     * Пример: "USD" - доллар США, "EUR" - евро
     */
    private final String charCode;
    
    /**
     * Текущий курс валюты к базовой валюте (например, к рублю)
     * Для отображения используется 4 знака после запятой
     */
    private final double rate;
    
    /**
     * Номинал - количество единиц валюты, к которому применяется курс.
     * Например, для доллара США номинал обычно 1, а для японской йены - 100.
     */
    private final int nominal;
    
    /**
     * Дата актуальности курса в формате строки.
     * Обычно используется формат ISO 8601 (YYYY-MM-DD)
     * Пример: "2023-05-15"
     */
    private final String date;

    /**
     * Конструктор класса.
     * 
     * @param charCode код валюты (3 буквы)
     * @param rate текущий курс
     * @param nominal номинал валюты
     * @param date дата актуальности курса
     */
    public CurrencyRateResponse(String charCode, double rate, int nominal, String date) {
        this.charCode = charCode;
        this.rate = rate;
        this.nominal = nominal;
        this.date = date;
    }

    /**
     * Преобразует объект в JSON-строку вручную.
     * Используется String.format для форматирования значений.
     * 
     * Пример возвращаемого JSON:
     * {
     *   "currency": "USD",
     *   "nominal": 1,
     *   "rate": 75.4560,
     *   "date": "2023-05-15"
     * }
     * 
     * Особенности:
     * - Используется text block (тройные кавычки) для удобного форматирования
     * - Метод strip() удаляет лишние пробелы в начале и конце
     * - Курс форматируется с 4 знаками после запятой
     * 
     * @return JSON-представление объекта
     */
    public String toJson() {
        return String.format(Locale.US, """
            {
              "charCode": "%s",
              "nominal": %d,
              "rate": %.4f,
              "date": "%s"
            }
            """.strip(), charCode, nominal, rate, date);
    }
    
    // Примечание: В реальных проектах для генерации JSON обычно используют
    // библиотеки (Jackson, Gson), но здесь показана ручная реализация
    // для понимания принципов работы.
}