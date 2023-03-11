FROM eclipse-temurin:11-alpine
MAINTAINER felis.ovh
COPY build/libs/discoveryClient-all.jar discoveryClient.jar
COPY keystore.jks keystore.jks
COPY src/main/resources/application-docker.conf application-docker.conf
EXPOSE 2137
EXPOSE 8100
ENTRYPOINT ["java","-jar","/discoveryClient.jar","-config=application-docker.conf"]
# , "-sslKeyStore=keystore.jks"
