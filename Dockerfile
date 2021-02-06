FROM openjdk:17-slim
MAINTAINER michael@mikuger.de

ENTRYPOINT ["/bin/sh", "-c"]

ADD ./build/distributions/dns-updater-1.0.tar /home/javarun

# ENTRYPOINT ["sh", "/home/javarun/app.jar/bin/dns-updater"]
#ENTRYPOINT ["sh", "/home/javarun/bin/dns-updater"]
ENTRYPOINT ["sh", "/home/javarun/dns-updater-1.0/bin/dns-updater"]