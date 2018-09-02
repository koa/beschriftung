FROM gcr.io/distroless/java
COPY target/beschriftung*.jar /app/app.jar
WORKDIR /app
VOLUME /data
EXPOSE 8080/tcp
CMD ["app.jar"]