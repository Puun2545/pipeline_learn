pipeline {
    agent any

    options {
        // เก็บ 20 build ล่าสุด และ 10 artifact ล่าสุด และเก็บ artifact ไว้ 7 วัน
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10', artifactDaysToKeepStr: '7'))
    }

    environment {
        // กำหนดเวอร์ชันของ Maven ให้เป็น 0.0.0 ตามด้วยเลข BUILD_NUMBER ที่ได้จาก Jenkins
        MAVEN_VERSION = "0.0.${BUILD_NUMBER}"
        MAVEN_HOME = tool 'M3' // ใช้ Maven ที่ติดตั้งใน Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/java-ci-test'
            }
        }

        stage('Build') {
            steps {
                script {
                    // สร้างไฟล์ version.properties ที่มีข้อมูลเวอร์ชันของ Maven
                    sh 'echo MAVEN_VERSION=${MAVEN_VERSION} > version.properties'
                }
                // ใช้ Maven ที่ติดตั้งใน Jenkins
                sh "${env.MAVEN_HOME}/bin/mvn clean package -Dproject.version=${MAVEN_VERSION}"
            }
        }
    }
    
    post {
        // ถ้า build สำเร็จ ให้เก็บ artifact ไว้
        success {
            archiveArtifacts artifacts: 'target/*.jar',
        }
    }
}
