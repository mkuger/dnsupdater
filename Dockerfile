FROM openjdk:17-slim
MAINTAINER michael@mikuger.de

ENTRYPOINT ["/bin/sh", "-c"]

COPY ./build/libs/dns-updater-1.0-SNAPSHOT.jar /home/javarun/app.jar

ENTRYPOINT ["java", "-jar", "/home/javarun/app.jar"]