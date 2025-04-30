package web.service;

import web.dto.CurrencyRateDTO;
import web.repository.CurrencyRateRepository;

import java.util.List;

/**
 * Сервисный класс, который управляет логикой получения данных.
 */
public class CurrencyRateService {
    private final CurrencyRateRepository repository = new CurrencyRateRepository();

    // Получить курс валюты на дату
    public List<CurrencyRateDTO> getLatestRate(String charCode) throws Exception {
        return repository.getRate(charCode);
    }

    // Получить курс валюты за период
    public List<CurrencyRateDTO> getRateHistory(String charCode, String from, String to) throws Exception {
        return repository.getRateHistory(charCode, from, to);
    }
}
