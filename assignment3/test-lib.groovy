@Library('my-shared-library@v0.0.1') _
testLib()

pipeline {
    agent {
        label 'slave-1'}

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
                // ไม่ได้มีประโยชน์อะไรเลย ทำเพื่อทดสอบเท่านั้น
                testCheckout(
                    branch: 'main', 
                    credentialsId: 'git-credential', 
                    url: 'https://github.com/Puun2545/java-ci-test'
                )
            }
        }

        stage('Build') {
            steps {
                // ใช้ Maven ที่ติดตั้งใน Jenkins
                echo "Loading testBuild function..."
                testBuild('${MAVEN_VERSION}')
            }
        }
    }
    
    post {
        // ถ้า build สำเร็จ ให้เก็บ artifact ไว้
        success {
            archiveArtifacts artifacts: 'target/*.jar'
        }
    }
}

