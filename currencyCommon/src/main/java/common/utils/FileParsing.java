package common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import common.model.ForeignCurrencyLibDto;
import common.model.ForeignCurrencyMarketDto;

public class FileParsing {
    
    /**
 * Метод для парсинга XML-файлов.
 * Определяет тип файла по его имени и парсит содержимое в соответствующие DTO объекты.
 * 
 * Поддерживает два типа файлов:
 * 1. lib.xml - файл с библиотекой валют (парсится в ForeignCurrencyLibDto)
 * 2. Другие имена - файлы с ежедневными курсами валют (парсится в ForeignCurrencyMarketDto)
 * 
 * @param filePath относительный путь к файлу от папки resources проекта
 * @return Список объектов DTO (может содержать ForeignCurrencyLibDto или ForeignCurrencyMarketDto)
 * @throws Exception если файл не найден или возникает ошибка при парсинге XML
 */
public static List<Object> fileParsing(String filePath) throws Exception {
    // Формируем абсолютный путь к файлу относительно проекта
    Path fullPath = Paths.get("resources", filePath).toAbsolutePath();
    File xmlFile = fullPath.toFile();

    // Проверяем существование файла
    if (!xmlFile.exists()) {
        throw new FileNotFoundException("Файл не найден: " + fullPath);
    }

    List<Object> result = new ArrayList<>();
    // Создаем парсер XML
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(xmlFile);

    // Получаем имя файла для определения типа данных
    String fileName = xmlFile.getName();

    if (fileName.equalsIgnoreCase("lib.xml")) {
        // Парсинг файла библиотеки валют
        NodeList itemList = doc.getElementsByTagName("Item");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // Создаем DTO для библиотеки валют и заполняем его поля
            ForeignCurrencyLibDto dto = new ForeignCurrencyLibDto();
            dto.setId(item.getAttribute("ID"));
            dto.setName(item.getElementsByTagName("Name").item(0).getTextContent());
            dto.setEngName(item.getElementsByTagName("EngName").item(0).getTextContent());
            dto.setNominal(parseIntSafe(item.getElementsByTagName("Nominal").item(0).getTextContent()));
            dto.setParentCode(parseIntSafe(item.getElementsByTagName("ParentCode").item(0).getTextContent()));

            result.add(dto);
        }
    } else {
        // Парсинг файла ежедневных курсов валют
        NodeList valuteList = doc.getElementsByTagName("Valute");
        // Получаем дату из корневого элемента
        String dateAttr = ((Element) doc.getDocumentElement()).getAttribute("Date");
        LocalDate dateReq = parseDate(dateAttr);

        for (int i = 0; i < valuteList.getLength(); i++) {
            Element valute = (Element) valuteList.item(i);

            // Создаем DTO для курса валют и заполняем его поля
            ForeignCurrencyMarketDto dto = new ForeignCurrencyMarketDto();
            dto.setValuteId(valute.getAttribute("ID"));
            dto.setNumCode(parseIntSafe(getTagValue(valute, "NumCode")));
            dto.setCharCode(getTagValue(valute, "CharCode"));
            dto.setValue(new BigDecimal(getTagValue(valute, "Value").replace(",", ".")));
            dto.setVunitRate(new BigDecimal(getTagValue(valute, "VunitRate").replace(",", ".")));
            dto.setDateReq(dateReq);

            result.add(dto);
        }
    }

    return result;
}

/**
 * Вспомогательный метод для получения текстового содержимого тега
 * @param element родительский XML-элемент
 * @param tagName имя искомого тега
 * @return текстовое содержимое тега
 */
private static String getTagValue(Element element, String tagName) {
    return element.getElementsByTagName(tagName).item(0).getTextContent();
}

/**
 * Безопасный парсинг строки в целое число
 * @param value строка для парсинга
 * @return целое число (0 если строка пустая или содержит только нечисловые символы)
 */
private static int parseIntSafe(String value) {
    if (value == null || value.isBlank()) {
        return 0;
    }
    return Integer.parseInt(value.trim().replaceAll("\\D+", ""));
}

/**
 * Парсинг десятичного числа в целое с округлением
 * @param value строка с десятичным числом (разделитель может быть точкой или запятой)
 * @return округленное целое число (0 если строка пустая)
 */
/*private static int parseDecimalToInt(String value) {
    if (value == null || value.isBlank()) {
        return 0;
    }
    // Заменяем запятую на точку и конвертируем
    value = value.replace(",", ".");
    double doubleValue = Double.parseDouble(value);
    return (int) Math.round(doubleValue);
}
*/

/**
 * Парсинг даты из строки формата dd.MM.yyyy
 * @param dateStr строка с датой
 * @return объект LocalDate
 */
private static LocalDate parseDate(String dateStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    return LocalDate.parse(dateStr, formatter);
}

}
