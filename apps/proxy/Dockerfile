# Build
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/proxy-0.0.1-SNAPSHOT.jar /usr/local/lib/proxy.jar

ARG newRelicAppName
ARG newRelicLicenseKey

ENV NEW_RELIC_APP_NAME=$newRelicAppName
ENV NEW_RELIC_LICENSE_KEY=$newRelicLicenseKey
ENV NEW_RELIC_LOG_FILE_NAME="STDOUT"

ADD newrelic/newrelic.jar /usr/local/lib
ADD newrelic/newrelic.yml /usr/local/lib

RUN sed -i s/"NEW_RELIC_APP_NAME"/$newRelicAppName/g /usr/local/lib/newrelic.yml
RUN sed -i s/"NEW_RELIC_LICENSE_KEY"/$newRelicLicenseKey/g /usr/local/lib/newrelic.yml

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:/usr/local/lib/newrelic.jar","-jar","/usr/local/lib/proxy.jar"]
