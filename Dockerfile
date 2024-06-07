FROM jenkins/jenkins:lts-jdk11
USER root
RUN apt update 
RUN apt install -y --no-install-recommends gnupg curl ca-certificates apt-transport-https
RUN curl -sSfL https://apt.octopus.com/public.key | apt-key add -
RUN sh -c "echo deb https://apt.octopus.com/ stable main > /etc/apt/sources.list.d/octopus.com.list"
RUN apt update
RUN apt-get install -y octopuscli
RUN jenkins-plugin-cli --plugins octopusdeploy:3.1.6
USER jenkins