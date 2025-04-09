FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="hong123 <gimdoyun@gmail.com>"
LABEL version="1.0"
COPY ./target/university-0.0.1-SNAPSHOT.jar /root
ARG BUILD_PROFILE=dev
ARG BUILD_PORT=8088
ENV TZ=Asia/Seoul
ENV APP_PROFILE=${BUILD_PROFILE}
EXPOSE ${BUILD_PORT}
WORKDIR /root
CMD ["java", "-jar", "university-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=${APP_PROFILE}"]