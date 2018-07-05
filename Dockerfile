FROM maven:3.5.3-jdk-8-alpine as build-env

ARG MAVEN_PROXY
ENV MAVEN_PROXY ${MAVEN_PROXY:-x}
RUN mkdir $HOME/.m2
RUN ([ $MAVEN_PROXY != "x" ] && echo "<settings><mirrors><mirror><id>proxy</id><name>proxy</name><mirrorOf>*</mirrorOf><url>$MAVEN_PROXY</url></mirror></mirrors></settings>" >>$HOME/.m2/settings.xml);echo
ADD . /build/app
WORKDIR /build/app

RUN mvn clean install
RUN mkdir -p /app
RUN mv target/beschriftung*.jar /app/app.jar

FROM gcr.io/distroless/java
COPY --from=build-env /app /app
WORKDIR /app
VOLUME /data
EXPOSE 8080/tcp
CMD ["app.jar"]