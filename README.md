# Story Field Backend

## 📖 프로젝트 소개
경기도 갭이어 프로젝트 '이야기 밭' 백엔드 입니다.

## 🛠️ 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **ORM**: JPA/Hibernate
- **Build Tool**: Gradle
- **Authentication**: Spring Security + JWT
- **API Documentation**: Swagger/OpenAPI 3
- **Testing**: JUnit 5, MockMvc

## 📁 프로젝트 구조
```
story-field-be/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── storyfield/
│   │   │           ├── StoryFieldApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   └── SwaggerConfig.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── StoryController.java
│   │   │           │   └── UserController.java
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── StoryService.java
│   │   │           │   └── UserService.java
│   │   │           ├── repository/
│   │   │           │   ├── StoryRepository.java
│   │   │           │   └── UserRepository.java
│   │   │           ├── entity/
│   │   │           │   ├── Story.java
│   │   │           │   └── User.java
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           └── common/
│   │   │               ├── exception/
│   │   │               └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
└── build.gradle
```

## 🚀 설치 및 실행

### 사전 요구사항
- Java 17
- MySQL 8.0
- Git
- Docker & Docker Compose



## 🐳 Docker로 실행 (권장)
### 1. 저장소 클론
```
git clone https://github.com/Gyeongditor/story-field-be.git
cd story-field-be
```

### 2. Docker Compose로 실행
```
docker-compose up -d
```
### 3. 로그 확인
```
docker-compose logs -f
```
### 4. 서비스 중지
```
docker-compose down
```
### 설치 방법
```bash
# 1. 저장소 클론
git clone https://github.com/Gyeongditor/story-field-be.git
cd story-field-be

# 2. 의존성 설치
./gradlew build

# 3. 데이터베이스 설정 (application.yml 수정)
# database 정보 입력

# 4. 애플리케이션 실행
./gradlew bootRun
```

### 개발 환경 실행
```bash
# 개발 모드로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 🔧 환경 설정

### application.yml 예시
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/story_field
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

jwt:
  secret: your-secret-key
  expiration: 86400000 # 24시간
```

## 📚 API 문서
서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 3 문서: `http://localhost:8080/v3/api-docs`

## 🧪 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.storyfield.service.StoryServiceTest"
```

## 🌟 주요 기능
- 사용자 인증 및 권한 관리
- 스토리 CRUD 기능
- JWT 기반 인증
- RESTful API 제공

## ➕ 추가 예정 기능
- AI로 생성된 이미지 저장

## 🤝 기여 방법
1. 이 저장소를 Fork 합니다
2. 새로운 기능 브랜치를 만듭니다 (`git checkout -b feature/new-feature`)
3. 변경사항을 커밋합니다 (`git commit -am 'Add new feature'`)
4. 브랜치에 Push 합니다 (`git push origin feature/new-feature`)
5. Pull Request를 생성합니다

## 📝 라이선스
이 프로젝트는 MIT 라이선스를 따릅니다.

## 👥 팀원
- 백승일 (@github-SeungIlB)
- 성연준 (@github-Mr-Sung98)

## 📞 문의사항
프로젝트에 대한 문의사항이 있으시면 Issues를 통해 연락주세요.