# Jenkins controller with our plugins pre-installed.
FROM jenkins/jenkins:lts-jdk17

# Install plugins at build time so the controller comes up ready to go.
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Skip the setup wizard; we configure Jenkins through the UI / JCasC instead.
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
