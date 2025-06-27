FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/storyfield-0.0.1-SNAPSHOT.jar /app.jar
COPY wait-for-it.sh ./wait-for-it.sh

RUN chmod +x ./wait-for-it.sh && \
    apt-get update && apt-get install -y netcat bash && rm -rf /var/lib/apt/lists/*

# Build-time args
ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD

# Run-time environment variables
ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}

CMD ["./wait-for-it.sh", "mysql:3306", "--timeout", "30", "--", "java", "-jar", "/app.jar"]
