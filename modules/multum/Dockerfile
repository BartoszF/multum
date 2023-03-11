FROM eclipse-temurin:11-alpine
MAINTAINER felis.ovh
COPY build/libs/multum-all.jar multum.jar
COPY keystore.jks keystore.jks
COPY src/main/resources/application.conf application.conf
EXPOSE 8443
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/multum.jar","-config=application.conf"]
#, "-sslKeyStore=keystore.jks"
