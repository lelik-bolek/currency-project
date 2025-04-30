package web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO — Data Transfer Object.
 * Используется для хранения информации о курсе валюты, полученной с currencyApi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)  //если поле есть в JSON, но не в классе — просто игнорируй его
public class CurrencyRateDTO {
    @JsonProperty("nominal")
    private int nominal;        // Номинал например, "10 юаней = 130 рублей"

    @JsonProperty("currency")
    private String currency;     // Валюта (например, USD)

    @JsonProperty("charCode")
    private String charCode;     // Код валюты (например, USD)

    @JsonProperty("date")
    private String date;         // Дата курса (в формате YYYY-MM-DD)

    @JsonProperty("rate")
    private double rate;         // Курс валюты к рублю

    // Геттеры и сеттеры — нужны для работы JSON-библиотек и удобства доступа
    public int getNominal() {
        return nominal;
    }
    
    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
