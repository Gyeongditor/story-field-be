FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/storyfield-0.0.1-SNAPSHOT.jar /app.jar
COPY wait-for-it.sh ./wait-for-it.sh

RUN chmod +x ./wait-for-it.sh && \
    apt-get update && apt-get install -y netcat bash && rm -rf /var/lib/apt/lists/*

CMD ["./wait-for-it.sh", "mysql:3306", "--timeout", "30", "--", "java", "-jar", "/app.jar"]
