FROM openjdk:13-alpine
MAINTAINER michael@mikuger.de
ADD build/distributions/dns-updater-1.0-SNAPSHOT.tar .
CMD ["sh",  "dns-updater-1.0-SNAPSHOT/bin/dns-updater"]