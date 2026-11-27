FROM node:24-bookworm-slim AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
ARG VITE_GOOGLE_CLIENT_ID
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend
WORKDIR /build
COPY backend/ ./
COPY --from=frontend /build/dist/ src/main/resources/static/
RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /build/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=production
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
