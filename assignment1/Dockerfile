FROM jenkins/inbound-agent:latest

USER root
# ติดตั้ง dependencies เพิ่มเติมที่ต้องการ
RUN apt-get update && apt-get install -y maven

USER jenkins
