FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
# We assume the jar is built outside, or we can copy and run maven 
# But standard practice for docker-compose with 'build: .' is to build it via maven wrapper
# To simplify, we will use a multi-stage maven build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY command-service/pom.xml command-service/pom.xml
COPY query-service/pom.xml query-service/pom.xml
# Need to copy source
COPY command-service/src command-service/src
RUN ./mvnw -pl command-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/command-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
