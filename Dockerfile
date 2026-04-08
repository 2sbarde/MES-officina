FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 🔥 verifica che il jar esista (DEBUG)
RUN ls target

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]