FROM eclipse-temurin:17-jdk-jammy
COPY . .
RUN ./gradlew build -x test
# 아래 줄의 파일명(pingpong-0.0.1-SNAPSHOT.jar)은 실제 이름과 같아야 합니다.
CMD ["java", "-jar", "build/libs/pingpong-0.0.1-SNAPSHOT.jar"]
