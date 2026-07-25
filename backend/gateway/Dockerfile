FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY services/user-service/pom.xml services/user-service/
COPY services/song-service/pom.xml services/song-service/
COPY services/playlist-service/pom.xml services/playlist-service/
COPY services/search-service/pom.xml services/search-service/
COPY services/song-process-service/pom.xml services/song-process-service/
COPY gateway/pom.xml gateway/
RUN mvn dependency:go-offline -B -q
COPY gateway/src gateway/src
RUN mvn -pl gateway -am package -Dmaven.test.skip=true -B -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
