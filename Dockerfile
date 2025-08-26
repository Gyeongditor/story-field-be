# 런타임만 필요하니 JRE 슬림으로
FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR & wait-for-it 스크립트만 포함
COPY build/libs/storyfield-*.jar /app/app.jar
COPY wait-for-it.sh /usr/local/bin/wait-for-it.sh

RUN chmod +x /usr/local/bin/wait-for-it.sh \
 && apt-get update \
 && apt-get install -y --no-install-recommends netcat bash \
 && rm -rf /var/lib/apt/lists/*

# 비밀/환경값은 절대 이미지에 넣지 않음
EXPOSE 8080
USER 1001

# DB 준비될 때까지 대기 (DB_HOST/DB_PORT는 .env에서 주입)
ENTRYPOINT ["sh","-c","wait-for-it.sh ${DB_HOST:-mysql}:${DB_PORT:-3306} --timeout 60 -- java -jar /app/app.jar"]
