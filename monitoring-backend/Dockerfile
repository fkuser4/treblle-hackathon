FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace/app

COPY ../pom.xml ../pom.xml
COPY pom.xml .
COPY src src

COPY ../monitoring-sdk ../monitoring-sdk

RUN --mount=type=cache,target=/root/.m2 \
    apk add --no-cache maven && \
    mvn install -pl ../monitoring-sdk -am -DskipTests && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]