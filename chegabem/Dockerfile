FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline || true

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

ENV TZ=America/Sao_Paulo

RUN useradd --create-home --shell /bin/false chegabem
USER chegabem

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-jar", "app.jar"]
