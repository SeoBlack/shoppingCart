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

# Copy JDBC driver JAR for runtime classpath in container
RUN mvn -q dependency:copy \
        -Dartifact=com.mysql:mysql-connector-j:8.3.0 \
        -DoutputDirectory=./libs


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
        x11-utils  \
        libgtk-3-0 \
        libglib2.0-0 \
        libpango-1.0-0 \
        libpangocairo-1.0-0 \
        libcairo2  \
        libgdk-pixbuf-2.0-0 \
        libatk1.0-0 \
        libatk-bridge2.0-0 \
        libatspi2.0-0 \
        libx11-6   \
        libxext6   \
        libxrender1 \
        libxrandr2 \
        libxcomposite1 \
        libxdamage1 \
        libxfixes3 \
        libxkbcommon0 \
        libxtst6   \
        libxi6     \
        libnss3    \
        libasound2t64 \
        libgl1     && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar  app.jar
COPY --from=build /app/javafx        /app/javafx
COPY --from=build /app/libs          /app/libs
COPY docker-entrypoint.sh            /app/start.sh
RUN chmod +x /app/start.sh

# VNC viewer connects to this port – no X server needed on the host
EXPOSE 5900

ENTRYPOINT ["/app/start.sh"]
