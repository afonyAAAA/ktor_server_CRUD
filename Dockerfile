FROM adoptopenjdk:11-jre-hotspot

WORKDIR /src

COPY --from=build /libs/atomic_cinema_ru.atomic_cinema_backend-all.jar .

EXPOSE 8080

CMD ["java", "-jar", "/atomic_cinema_ru.atomic_cinema_backend-all.jar"]