# # ===== Build Stage =====
# FROM eclipse-temurin:21-jdk-alpine AS builder

# WORKDIR /app

# # Gradle Wrapper 복사 (캐싱 최적화)
# COPY gradlew .
# COPY gradle gradle
# RUN chmod +x ./gradlew

# # 의존성 파일만 먼저 복사 (레이어 캐싱)
# COPY build.gradle .
# COPY settings.gradle .

# # 의존성 다운로드 (소스 변경 시에도 캐시 활용)
# RUN ./gradlew dependencies --no-daemon || true

# # 소스 코드 복사
# COPY src src

# # 빌드 (테스트 스킵)
# RUN ./gradlew bootJar --no-daemon -x test

# # ===== Runtime Stage =====
# FROM eclipse-temurin:21-jre-alpine

# WORKDIR /app

# # 보안: 비root 유저 생성
# RUN addgroup -g 1001 -S appgroup && \
#     adduser -u 1001 -S appuser -G appgroup

# # 타임존 설정
# RUN apk add --no-cache tzdata && \
#     cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
#     echo "Asia/Seoul" > /etc/timezone

# # 빌드된 JAR 복사
# COPY --from=builder /app/build/libs/*.jar app.jar

# # 소유권 변경
# RUN chown appuser:appgroup app.jar

# # 비root 유저로 전환
# USER appuser

# # 헬스체크
# # HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
# #     CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# EXPOSE 8080

# # JVM 최적화 옵션
# ENTRYPOINT ["java", \
#     "-XX:+UseContainerSupport", \
#     "-XX:MaxRAMPercentage=75.0", \
#     "-XX:InitialRAMPercentage=50.0", \
#     "-Djava.security.egd=file:/dev/./urandom", \
#     "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# CI에서 만들어 둔 jar만 복사
COPY build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
