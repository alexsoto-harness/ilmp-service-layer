FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
ARG IMAGE_TAG=dev
ARG GIT_SHA=unknown
ARG BUILD_NUMBER=0
ENV IMAGE_TAG=${IMAGE_TAG} GIT_SHA=${GIT_SHA} BUILD_NUMBER=${BUILD_NUMBER}
RUN useradd --uid 10001 --no-create-home svc
WORKDIR /srv
COPY --from=build /src/target/service-layer.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/srv/app.jar"]
