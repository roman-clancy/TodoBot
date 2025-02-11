# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-jammy as builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Gradle Wrapper
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Собираем приложение с помощью Gradle
RUN ./gradlew build

# Используем базовый образ с JRE 17 для финального образа
FROM eclipse-temurin:17-jre-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из предыдущего этапа
COPY --from=builder /app/build/libs/*.jar ./app.jar

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]