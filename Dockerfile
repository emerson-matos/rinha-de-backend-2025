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
RUN lein uberjar

# --
# Final image
FROM openjdk:21-jdk
WORKDIR /app

# Create a non-root user and group
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built uberjar
COPY --from=builder /app/target/backend-payment-processor-0.0.1-SNAPSHOT-standalone.jar app.jar

# Set ownership and permissions
RUN chown -R appuser:appuser /app && chmod 755 /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
