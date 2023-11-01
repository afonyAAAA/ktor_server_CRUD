FROM openjdk:11-jre-slim

WORKDIR /src

COPY build/libs/com.example.ktor-server-all.jar /src/com.example.ktor-server-all.jar

EXPOSE 8080

CMD ["java", "-jar", "/src/com.example.ktor-server-all.jar"]