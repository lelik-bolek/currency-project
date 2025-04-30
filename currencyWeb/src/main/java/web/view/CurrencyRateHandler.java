package web.view;

import web.dto.CurrencyRateDTO;
import web.service.CurrencyRateService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * Обработчик запроса от пользователя.
 * Отображает веб-страницу и вызывает методы сервиса.
 */
public class CurrencyRateHandler implements HttpHandler {
    private final CurrencyRateService service = new CurrencyRateService();

    // Словарь с кодами валют и их полными наименованиями на русском языке
    private static final Map<String, String> currencyNames = new HashMap<>();
    static {
    currencyNames.put("AUD", "Австралийский доллар");
    currencyNames.put("AZN", "Азербайджанский манат");
    currencyNames.put("GBP", "Фунт стерлингов");
    currencyNames.put("AMD", "Армянских драмов");
    currencyNames.put("BYN", "Белорусский рубль");
    currencyNames.put("BGN", "Болгарский лев");
    currencyNames.put("BRL", "Бразильский реал");
    currencyNames.put("HUF", "Форинтов");
    currencyNames.put("VND", "Донгов");
    currencyNames.put("HKD", "Гонконгский доллар");
    currencyNames.put("GEL", "Лари");
    currencyNames.put("DKK", "Датская крона");
    currencyNames.put("AED", "Дирхам ОАЭ");
    currencyNames.put("USD", "Доллар США");
    currencyNames.put("EUR", "Евро");
    currencyNames.put("EGP", "Египетских фунтов");
    currencyNames.put("INR", "Индийских рупий");
    currencyNames.put("IDR", "Рупий");
    currencyNames.put("KZT", "Тенге");
    currencyNames.put("CAD", "Канадский доллар");
    currencyNames.put("QAR", "Катарский риал");
    currencyNames.put("KGS", "Сомов");
    currencyNames.put("CNY", "Юань");
    currencyNames.put("MDL", "Молдавских леев");
    currencyNames.put("NZD", "Новозеландский доллар");
    currencyNames.put("NOK", "Норвежских крон");
    currencyNames.put("PLN", "Злотый");
    currencyNames.put("RON", "Румынский лей");
    currencyNames.put("XDR", "СДР (специальные права заимствования)");
    currencyNames.put("SGD", "Сингапурский доллар");
    currencyNames.put("TJS", "Сомони");
    currencyNames.put("THB", "Батов");
    currencyNames.put("TRY", "Турецких лир");
    currencyNames.put("TMT", "Новый туркменский манат");
    currencyNames.put("UZS", "Узбекских сумов");
    currencyNames.put("UAH", "Гривен");
    currencyNames.put("CZK", "Чешских крон");
    currencyNames.put("SEK", "Шведских крон");
    currencyNames.put("CHF", "Швейцарский франк");
    currencyNames.put("RSD", "Сербских динаров");
    currencyNames.put("ZAR", "Рэндов");
    currencyNames.put("KRW", "Вон");
    currencyNames.put("JPY", "Иен");
	}

    @Override
    public void handle(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            Map<String, String> queryParams = parseQueryParams(uri.getQuery());

            String path = uri.getPath();
            String responseHtml;

            // Если запрашивается текущий курс
            if (path.equals("/rate") && queryParams.containsKey("charCode")) {
                List<CurrencyRateDTO> rates = service.getLatestRate(queryParams.get("charCode"));
                responseHtml = buildHtml("Курс выбранной валюты, по отношению к рублю RU", rates);
            } 
            // Если запрашивается история курсов
            else if (path.equals("/rate-history") && queryParams.containsKey("charCode")) {
                List<CurrencyRateDTO> rates = service.getRateHistory(
                    queryParams.get("charCode"),
                    queryParams.getOrDefault("from", ""),
                    queryParams.getOrDefault("to", "")
                );
                responseHtml = buildHtml("Динамика курса выбранной валюты, по отношению к рубли RU, за период", rates);
            } 
            // Форма для ввода
            else {
                responseHtml = buildFormHtml();
            }

            byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Парсим параметры из URI
    private Map<String, String> parseQueryParams(String query) {
        if (query == null) return Map.of();
        return List.of(query.split("&")).stream()
            .map(p -> p.split("="))
            .filter(p -> p.length == 2)
            .collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    // Строим HTML для отображения курсов
    private String buildHtml(String title, List<CurrencyRateDTO> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1'><style>");
        sb.append("body{font-family:sans-serif;padding:20px} table{width:100%;border-collapse:collapse;} td,th{border:1px solid #ccc;padding:8px}");
        sb.append("</style></head><body>");
        sb.append("<h2>").append(title).append("</h2><table><tr><th>Дата</th><th>Валюта</th><th>Курс</th></tr>");
        for (CurrencyRateDTO dto : list) {
            // Используем полное наименование валюты, а для API отправляем только код валюты
            String fullCurrencyName = currencyNames.get(dto.getCharCode());
            sb.append("<tr><td>").append(dto.getDate()).append("</td>")
              .append("<td>").append(fullCurrencyName != null ? fullCurrencyName : dto.getCharCode()).append("</td>")
              .append("<td>").append(dto.getRate()).append("</td></tr>");
        }
        sb.append("</table><br><a href='/'>Назад</a></body></html>");
        return sb.toString();
    }

    // Строим форму для выбора валюты и даты/периода
    private String buildFormHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>")
          .append("<style>")
          .append("body{font-family:sans-serif;padding:20px}")
          .append("form{margin-bottom:20px}")
          .append("label{display:block;margin-top:10px}")
          .append("input,select{padding:5px;font-size:16px}")
          .append("</style>")
          .append("</head><body>")
          .append("<h1>Курсы валют</h1>");

        // Форма для запроса текущего курса
        sb.append("<form action='/rate' method='get'>")
          .append("<label>Выберите валюту:</label>")
          .append("<select name='charCode'>");

        // Перебираем все валюты и выводим их с полными наименованиями в выпадающем списке
        currencyNames.forEach((code, name) -> {
            sb.append("<option value='").append(code).append("'>")
              .append(name).append("</option>");
        });

        sb.append("</select><button type='submit'>Курс к рублю RU</button></form>");

        // Форма для запроса динамики курса
        sb.append("<form action='/rate-history' method='get'>")
          .append("<label>Выберите валюту:</label>")
          .append("<select name='charCode'>");

        currencyNames.forEach((code, name) -> {
            sb.append("<option value='").append(code).append("'>")
              .append(name).append("</option>");
        });

        sb.append("</select><label>С даты: <input type='date' name='from' required></label>")
          .append("<label>По дату: <input type='date' name='to' required></label>")
          .append("<button type='submit'>Показать динамику</button></form>")
          .append("</body></html>");
        return sb.toString();
    }
}
