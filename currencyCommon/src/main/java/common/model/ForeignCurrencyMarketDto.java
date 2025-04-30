package common.model;

import java.math.BigDecimal;
import java.time.LocalDate;


public class ForeignCurrencyMarketDto {
    private int id;
    private String valuteId;
    private int numCode;
    private String charCode;
    private BigDecimal value;
    private BigDecimal vunitRate;
    private LocalDate dateReq;

// Пустой конструктор (обязательный для сериализации/десериализации)
public ForeignCurrencyMarketDto() {

}

    // Конструктор
public ForeignCurrencyMarketDto(int id, String valuteId, int numCode, String charCode, BigDecimal  value, BigDecimal  vunitRate, LocalDate dateReq) {
    this.id = id;
    this.valuteId = valuteId;
    this.numCode = numCode;
    this.charCode = charCode;
    this.value = value;
    this.vunitRate = vunitRate;
    this.dateReq = dateReq;
    }

// Геттеры и сеттеры
public int getId() { return id;}
public void setId(int id) { this.id = id;}

public String getValuteId() { return valuteId;}
public void setValuteId(String valuteId) { this.valuteId = valuteId;}

public int getNumCode() { return numCode;}
public void setNumCode(int numCode) { this.numCode = numCode;}

public String getCharCode() { return charCode;}
public void setCharCode(String charCode) { this.charCode = charCode;}

public BigDecimal  getValue() { return value;}
public void setValue(BigDecimal value) { this.value = value;}

public BigDecimal  getVunitRate() { return vunitRate;}
public void setVunitRate(BigDecimal vunitRate) { this.vunitRate = vunitRate;}

public LocalDate getDateReq() { return dateReq;}
public void setDateReq(LocalDate dateReq) { this.dateReq = dateReq;}
}