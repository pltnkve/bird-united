FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/twitter-1.3.jar app.jar

EXPOSE 9001

ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=mysql-twitter
ENV MYSQL_PORT=3306
ENV MYSQL_DATABASE=twitter
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=root
ENV JWT_SECRET=passw

ENTRYPOINT ["java", "-jar", "app.jar"]
