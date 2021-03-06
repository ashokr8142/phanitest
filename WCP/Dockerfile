# Package WCP/fdahpStudyDesigner
FROM maven:3.6.3-jdk-11-openj9 AS maven_builder
COPY fdahpStudyDesigner/pom.xml /app/
COPY fdahpStudyDesigner/src /app/src/
WORKDIR /app/
RUN mvn package
RUN ls /app/target

FROM tomcat:9.0.33-jdk11-openjdk

# Creates mounted path for gcsfuse
RUN mkdir /usr/local/tomcat/webapps/fdaResources
RUN chmod a+w /usr/local/tomcat/webapps/fdaResources
RUN mkdir /usr/local/tomcat/webapps/fdaResources/studyResources

# Installs gcsfuse
ENV GCSFUSE_REPO gcsfuse-stretch
RUN apt-get update && apt-get install --yes --no-install-recommends \
    ca-certificates \
    curl \
    gnupg \
  && echo "deb http://packages.cloud.google.com/apt $GCSFUSE_REPO main" \
    | tee /etc/apt/sources.list.d/gcsfuse.list \
  && curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \
  && apt-get update \
  && apt-get install --yes gcsfuse \
  && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=maven_builder /app/target/studybuilder.war /usr/local/tomcat/webapps/
# Update conf/server.xml to remove remote IP address from access logs.
# See https://tomcat.apache.org/tomcat-6.0-doc/config/valve.html#Access_Log_Valve
RUN sed -i "s/pattern=\"%h %l %u %t &quot;%r&quot; %s %b\"/pattern=\"%t %r %s %b\"/g" /usr/local/tomcat/conf/server.xml
CMD ["catalina.sh","run"]
