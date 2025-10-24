FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml /workspace/pom.xml
RUN mvn -N install

COPY monitoring-sdk /workspace/monitoring-sdk
RUN cd monitoring-sdk && mvn clean install -DskipTests

COPY demo-application /workspace/demo-application
RUN cd demo-application && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/demo-application/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]