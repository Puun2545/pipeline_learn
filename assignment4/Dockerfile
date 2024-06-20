# Use the official Sonatype Nexus image as the base image
FROM sonatype/nexus3

# Install SSH, cURL, and other common tools using dnf
USER root
RUN dnf update -y && \
    dnf install -y openssh-server curl git maven docker && \
    dnf clean all

# Set up SSH
RUN ssh-keygen -A && \
    echo "root:password" | chpasswd

# Create Jenkins user and set up working directory
RUN useradd -m -d /home/jenkins jenkins && \
    mkdir -p /home/jenkins/agent && \
    chown -R jenkins:jenkins /home/jenkins

# Set environment variables for Jenkins
ENV JENKINS_HOME /home/jenkins
ENV JENKINS_AGENT_HOME /home/jenkins/agent

# Switch to Jenkins user
USER jenkins
WORKDIR /home/jenkins/agent

# Expose SSH and Nexus ports
EXPOSE 22 8081

# Start SSH service and Nexus
CMD /usr/sbin/sshd && /opt/sonatype/start-nexus-repository-manager.sh
