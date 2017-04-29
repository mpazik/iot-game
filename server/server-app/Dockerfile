FROM anapsix/alpine-java:jre8

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY target/*.jar server.jar
COPY target/dependency/ lib/
COPY start-app.sh .

EXPOSE 7000 7001

CMD ["/bin/ash", "start-app.sh"]