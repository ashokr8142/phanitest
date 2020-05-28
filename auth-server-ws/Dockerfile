FROM maven:3.6.3-jdk-11-openj9 AS maven_builder
COPY pom.xml /app/
COPY src /app/src/
WORKDIR /app/
RUN mvn package

FROM tomcat:9.0.33-jdk11-openjdk
COPY --from=maven_builder /app/target/AuthServer.war /usr/local/tomcat/webapps/
# Update conf/server.xml to remove remote IP address from access logs.
# See https://tomcat.apache.org/tomcat-6.0-doc/config/valve.html#Access_Log_Valve
RUN sed -i "s/pattern=\"%h %l %u %t &quot;%r&quot; %s %b\"/pattern=\"%t %r %s %b\"/g" /usr/local/tomcat/conf/server.xml
CMD ["catalina.sh","run"]
