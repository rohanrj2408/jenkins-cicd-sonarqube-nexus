# Build agent: the standard inbound agent (JDK 17) plus a pinned Maven, so the
# pipeline can just call `mvn` without any Jenkins tool configuration.
FROM jenkins/inbound-agent:latest-jdk17

USER root

# Pull Maven from the Apache archive (keeps every version, so the link never
# rots the way the dlcdn mirror does once a release ages out).
ARG MAVEN_VERSION=3.9.9
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      -o /tmp/maven.tar.gz \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven \
    && rm /tmp/maven.tar.gz

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

USER jenkins
