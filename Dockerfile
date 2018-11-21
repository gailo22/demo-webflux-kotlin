FROM amazon-corretto-8

EXPOSE 9090

RUN mkdir -p /app/

ADD build/libs/demo-webflux-kotlin-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]