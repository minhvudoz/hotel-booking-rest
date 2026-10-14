FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package

FROM tomcat:9.0-jre17-temurin
ENV TZ=Asia/Ho_Chi_Minh \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Ho_Chi_Minh"
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/hotel.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
