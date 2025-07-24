FROM clojure:temurin-21-lein-noble AS builder

WORKDIR /app

# Copy project files and download dependencies (for better caching)
COPY project.clj .
RUN mkdir -p src resources config
RUN lein deps

# Copy the rest of the source code
COPY src ./src
# COPY resources ./resources
COPY config ./config

# Build the uberjar
RUN lein deps
RUN lein uberjar

# --
# Final image
FROM openjdk:21-jdk
WORKDIR /app

# Create a non-root user and group
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built uberjar
COPY --from=builder /app/target/backend-payment-processor-*-standalone.jar app.jar

# Set ownership and permissions
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

# JVM tuning for low memory usage and performance
ENV JVM_OPTS="-Xmx120m \
              -Xms120m \
              -XX:+UseG1GC \
              -XX:MaxGCPauseMillis=20 \
              -XX:+UseStringDeduplication \
              -XX:+OptimizeStringConcat \
              -Djava.awt.headless=true \
              -Dfile.encoding=UTF-8 \
              -Duser.timezone=UTC \
              -server"

ENTRYPOINT java $JVM_OPTS -jar app.jar
