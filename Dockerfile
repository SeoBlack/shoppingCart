# ── Stage 1: Build ────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependency downloads separately from source changes
COPY pom.xml .
RUN mvn -q -e dependency:go-offline -DskipTests

COPY src ./src
RUN mvn clean package -DskipTests

# Download Linux-platform JavaFX JARs (include native rendering libraries).
# The plain JARs declared in pom.xml contain only Java bytecode; the
# linux-classifier JARs also bundle the platform .so files needed at runtime.
RUN mvn -q dependency:copy \
        -Dartifact=org.openjfx:javafx-base:17.0.6:jar:linux     \
        -DoutputDirectory=./javafx && \
    mvn -q dependency:copy \
        -Dartifact=org.openjfx:javafx-graphics:17.0.6:jar:linux \
        -DoutputDirectory=./javafx && \
    mvn -q dependency:copy \
        -Dartifact=org.openjfx:javafx-controls:17.0.6:jar:linux \
        -DoutputDirectory=./javafx && \
    mvn -q dependency:copy \
        -Dartifact=org.openjfx:javafx-fxml:17.0.6:jar:linux     \
        -DoutputDirectory=./javafx


# ── Stage 2: Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Xvfb   – virtual framebuffer (provides a display without a monitor)
# x11vnc – VNC server that exposes the virtual display on port 5900
# libgl1 – Mesa software renderer (GPU not required in the container)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        xvfb       \
        x11vnc     \
        libx11-6   \
        libxext6   \
        libxrender1 \
        libxtst6   \
        libxi6     \
        libgl1     && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar  app.jar
COPY --from=build /app/javafx        /app/javafx
COPY docker-entrypoint.sh            /app/start.sh
RUN chmod +x /app/start.sh

# VNC viewer connects to this port – no X server needed on the host
EXPOSE 5900

ENTRYPOINT ["/app/start.sh"]
