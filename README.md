# currency-project

Проект `currency-project` — это учебное Java-приложение, построенное по архитектуре микросервисов. Цель проекта — реализовать загрузку актуального курса валют с сайта ЦБ РФ, хранение и отображение этих данных с возможностью взаимодействия через API и веб-интерфейс. Используются H2 Database, XML-парсинг, HTTP API, веб-интерфейс и Docker.


## Архитектура и модули

Проект состоит из следующих микросервисов:

### 1. `currencyDatabase`
- **Роль:** инициализация базы данных H2.
- **Функции:**
  - Создание таблиц `ForeignCurrencyMarketLib`, `ForeignCurrencyMarket` и схемы `STAGING`.
  - Хранение всех данных о валютных курсах.
- **Связи:** доступен для чтения и записи из других сервисов через JDBC.

---

### 2. `currencyLoader`
- **Роль:** загрузка и обновление курсов валют.
- **Функции:**
  - Проверка актуальности данных.
  - Загрузка XML-файлов с сайта ЦБ РФ.
  - Парсинг и сохранение данных в базу.
- **Связи:** использует `currencyCommon` для парсинга, взаимодействует с `currencyDatabase` через JDBC.

---

### 3. `currencyWeb`
- **Роль:** веб-интерфейс для отображения данных.
- **Функции:**
  - HTML-страница для просмотра курсов валют.
  - Выбор даты и отображение курса по валютам.
- **Связи:** может напрямую обращаться к базе данных или использовать `currencyApi` как прокси.

---

### 4. `currencyApi`
- **Роль:** точка взаимодействия между сервисами через REST API.
- **Функции:**
  - `GET /currencies?date=` — список курсов на дату.
  - `GET /currency/{charCode}` — курс конкретной валюты.
  - `POST /load` — инициировать загрузку валют (не реализовано).
- **Связи:**
  - Взаимодействует с `currencyDatabase` и `currencyLoader`.
  - Используется `currencyWeb` для получения данных.

---

### 5. `currencyCommon`
- **Роль:** общая библиотека для всех модулей.
- **Функции:**
  - DTO классы (`Currency`, `CurrencyRate`, и т.д.).
  - XML-парсеры.
  - Утилиты: преобразование дат, логгирование, конфигурация.
- **Связи:** добавляется как зависимость в остальные модули.

---

## Схема взаимодействия микросервисов
```
             +--------------------+
             | currencyDatabase   |<-----------+
             +--------------------+            |
                      ▲                        |
                      |                        |
               (JDBC) |                        |
                      |                        v
+--------------+  REST API   +---------------------+
| currencyWeb  |<----------->|     currencyApi     |
+--------------+             +---------------------+
                                  ▲          ▲
                                  |          |
                           (call REST)   (REST/API trigger)
                                  |          |
                          +---------------+  |
                          | currencyLoader|--+
                          +---------------+
```

## Особенности проекта

- **Java 21**
- **Используется чистая Java** без автоконфигурации и сторонних фреймворков.
- **База данных H2** в виде встроенной БД (embedded) или TCP-сервер.
- **Maven multi-module** структура с модулями, оформленными как независимые сервисы.


## Структура проекта
```
currency-project/
├── README.md
├── pom.xml
├── docker-compose.yml                          # Для локального запуска всех сервисов

├── resources/                                  # директория с ресурсами
│   ├── logs/
│   ├── tmpfiles/								                  # директория для временного хранения файлов
│   		├── date/							                  # файлы дата.xml
│   		└── lib/							                  # файл lib.xml

                             
currencyCommon/									                  # Общие классы: DTO, парсеры XML, утилиты (не сервис)
└── src/main/java/common/
    ├── DatabaseManager.java   	
    ├── model/
    │   ├── ForeignCurrencyMarketDto.java
    │   └── ForeignCurrencyLibDto.java
    └── utils/									                  # Методы для работы с H2
        ├── DbUtils.java
        ├── FileParsing.java
        └── FileUtils.java

├── currencyLoader/                             # Микросервис загрузки и обновления данных
│   ├── pom.xml
│   └── src/main/resources/config.properties
│   └── src/main/java/loader/
│                 		├──LoaderApp.java
│                 		└──LoaderService.java

├── currencyApi/
│   └── src/main/java/api/
│       ├── ApiApp.java                         	# Главный класс запуска
│       ├── controller/
│       │   └── CurrencyRateController.java     	# Контроллер с GET-запросами
│       └── dto/
│           └── CurrencyRateResponse.java       	# DTO-ответ (название, дата, курс)

├── currencyWeb/
│   └── src/main/java/web/
│       ├── WebApp.java
│       ├── service/
│       │   └── CurrencyRateService.java        # Сервис для работы с курсами валют
│       ├── repository/
│       │   └── CurrencyRateRepository.java     # Репозиторий, который выполняет SQL-запросы
│       └── dto/
│           └── CurrencyRateDTO.java            # Внутреннее DTO

├── currencyDatabase/                           # Микросервис для инициализации и миграций
│   ├── pom.xml
│   ├── src/main/java/database/ 
│   │ 			├── DatabaseApp.java
│	  │			  └── DataDefinitionLanguage.java
│   └── src/main/resources/
│               ├── config.properties
│               └── ddl.sql
```

#### Инструкции по запуску
## Как запустить

1. Склонируйте репозиторий:

   git clone https://github.com/yourusername/currency-project.git
   cd currency-project


2. Соберите проект:

   mvn clean package


3. Запустите в Docker:

   docker-compose -f docker/docker-compose.yml up --build


4. Веб-интерфейс будет доступен по адресу: [http://localhost:8080](http://localhost:8080)

#### Технологии

## Технологии и инструменты

- Java 21, Maven
- H2 Database
- XML (JAXB)
- REST API (com.sun.net.httpserver)
- Docker, Docker Compose
- HTML/CSS для Web UI


#### Возможности и планы развития

## Что делает проект

- Загружает курсы валют с сайта ЦБ
- Сохраняет данные в H2 Database
- Предоставляет API и веб-интерфейс
- Работает как полноценная микросервисная система

## Возможные улучшения
- Добавление авторизации
- Перенос на PostgreSQL
- Расширение API
