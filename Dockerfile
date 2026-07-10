# =============================================
# ビルドステージ
# =============================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# =============================================
# 実行ステージ
# =============================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# タイムゾーン設定（日本時間）
ENV TZ=Asia/Tokyo
RUN apt-get update && apt-get install -y tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Duser.timezone=Asia/Tokyo", \
  "-jar", "app.jar"]
