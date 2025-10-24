FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml /workspace/pom.xml
RUN mvn -N install

COPY monitoring-sdk /workspace/monitoring-sdk
RUN cd monitoring-sdk && mvn clean install -DskipTests

COPY monitoring-backend /workspace/monitoring-backend
RUN cd monitoring-backend && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/monitoring-backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]