package common.model;


public class ForeignCurrencyLibDto {
    private String id;
    private String name;
    private String engName;
    private int nominal;
    private int parentCode;

// Пустой конструктор (обязательный для сериализации/десериализации)
public ForeignCurrencyLibDto() {
    
    }

// Конструктор
public ForeignCurrencyLibDto(String id, String name, String engName, int nominal, int parentCode) {
    this.id = id;
    this.name = name;
    this.engName = engName;
    this.nominal = nominal;
    this.parentCode = parentCode;
    }

// Геттеры и сеттеры
public String getId() { return id;}
public void setId(String id) { this.id = id;}

public String getName() { return name;}
public void setName(String name) { this.name = name;}

public String getEngName() { return engName;}
public void setEngName(String engName) { this.engName = engName;}

public int getNominal() { return nominal;}
public void setNominal(int nominal) { this.nominal = nominal;}

public int getParentCode() { return parentCode;}
public void setParentCode(int parentCode) { this.parentCode = parentCode;}

}