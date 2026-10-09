# ============================================================
# AegisComm — Multi-stage Docker Build
# Stage 1: Compile Java source with Maven-less javac approach
# Stage 2: Deploy WAR on Tomcat 9
# ============================================================

# --- Stage 1: Build the WAR ---
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

# Copy source tree
COPY src/main/java/ src/main/java/
COPY src/main/webapp/ src/main/webapp/

# Download dependencies (BCrypt is vendored in source)
RUN mkdir -p libs \
 && curl -sL -o libs/servlet-api.jar \
      "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar" \
 && curl -sL -o libs/mysql-connector.jar \
      "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar" \
 && curl -sL -o libs/jakarta-mail.jar \
      "https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar" \
 && curl -sL -o libs/jakarta-activation.jar \
      "https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar"

# Compile all Java sources
RUN mkdir -p build/classes \
 && find src/main/java -name "*.java" > sources.txt \
 && javac -d build/classes \
      -cp "libs/servlet-api.jar:libs/mysql-connector.jar:libs/jakarta-mail.jar:libs/jakarta-activation.jar" \
      @sources.txt

# Assemble WAR
RUN mkdir -p build/war/WEB-INF/classes build/war/WEB-INF/lib \
 && cp -r src/main/webapp/* build/war/ \
 && cp -r build/classes/* build/war/WEB-INF/classes/ \
 && cp libs/mysql-connector.jar build/war/WEB-INF/lib/ \
 && cp libs/jakarta-mail.jar build/war/WEB-INF/lib/ \
 && cp libs/jakarta-activation.jar build/war/WEB-INF/lib/ \
 && cd build/war && jar -cf /build/ROOT.war .

# --- Stage 2: Production Tomcat ---
FROM tomcat:9.0-jre17-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR
COPY --from=builder /build/ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

CMD ["catalina.sh", "run"]
