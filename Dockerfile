FROM openjdk:17-jdk-slim

WORKDIR /app
# JAR 및 쉘 스크립트 복사
COPY build/libs/storyfield-0.0.1-SNAPSHOT.jar /app.jar
COPY wait-for-it.sh ./wait-for-it.sh

RUN chmod +x ./wait-for-it.sh && \
    apt-get update && apt-get install -y netcat bash && rm -rf /var/lib/apt/lists/*

# Build-time args
# 빌드타임 ARG 선언 (CI/CD 시 전달됨)
ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
ARG JWT_SECRET
ARG SPRING_EMAIL_USERNAME
ARG SPRING_EMAIL_PASSWORD

# Run-time environment variables
# ARG -> ENV로 복사 (실행 시점 사용 가능)
ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV JWT_SECRET=${JWT_SECRET}
ENV SPRING_EMAIL_USERNAME=${SPRING_EMAIL_USERNAME}
ENV SPRING_EMAIL_PASSWORD=${SPRING_EMAIL_PASSWORD}

# wait-for-it으로 DB 준비 대기 후 jar 실행
CMD ["./wait-for-it.sh", "mysql:3306", "--timeout", "30", "--", "java", "-jar", "/app.jar"]
