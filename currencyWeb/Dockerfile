FROM eclipse-temurin:21-jdk

# Устанавливаем переменную среды для корректной кодировки логов
ENV LANG C.UTF-8

WORKDIR /app
COPY target/*.jar app.jar
# Каталог, в который будет примонтирован том (можно создать, но H2 сам создаст при необходимости)
VOLUME ["/h2data"]
ENTRYPOINT  ["java", "-jar", "app.jar"]