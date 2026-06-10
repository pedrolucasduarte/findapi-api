FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN ./mvnw --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:25-jre

RUN useradd --create-home --shell /usr/sbin/nologin findapi

WORKDIR /app

COPY --from=build /workspace/target/findapi-api-*.jar app.jar

USER findapi

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
