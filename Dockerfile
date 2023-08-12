FROM amazoncorretto:17-alpine

EXPOSE 8080

ARG JAVA_OPTION
ENV JAVA_OPTION=$JAVA_OPTION

RUN mkdir -p /app
COPY ./build/libs/* ./app/app.jar
WORKDIR /app

ENTRYPOINT "java" ${JAVA_OPTION} "-jar" "app.jar"