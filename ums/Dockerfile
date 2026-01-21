FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/ums-1.3.jar app.jar

EXPOSE 9000

ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=mysql-ums
ENV MYSQL_PORT=3306
ENV MYSQL_DATABASE=ums
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=root
ENV JWT_SECRET=passw

ENTRYPOINT ["java", "-jar", "app.jar"]
