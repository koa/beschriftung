FROM maven:3.6.0-jdk-11-slim as build-env
ADD . /build/beschriftung
WORKDIR /build/beschriftung
RUN mvn clean install
RUN mkdir -p /app
RUN mv target/beschriftung*.jar /app/beschriftung.jar
FROM gcr.io/distroless/java@sha256:da8aa0fa074d0ed9c4b71ad15af5dffdf6afdd768efbe2f0f7b0d60829278630
COPY --from=build-env /app /app
WORKDIR /app
CMD ["beschriftung.jar"]
EXPOSE 8080/tcp