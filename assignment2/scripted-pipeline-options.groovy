node {
    // กำหนดตัวเลือกต่างๆ ที่ใช้ใน pipeline
    properties([
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10', artifactDaysToKeepStr: '7'))
    ])

    // กำหนด environment
    def MAVEN_VERSION = "0.0.${env.BUILD_NUMBER}"
    def MAVEN_HOME = tool 'M3' // ใช้ Maven ที่ติดตั้งใน Jenkins

    try {
        stage('Checkout') {
            // checkout code จาก git
            git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/java-ci-test'
        }

        stage('Build') {
            // สร้างไฟล์ version.properties ที่มีข้อมูลเวอร์ชันของ Maven
            sh 'echo MAVEN_VERSION=${MAVEN_VERSION} > version.properties'
            // ใช้ Maven ที่ติดตั้งใน Jenkins
            sh "${MAVEN_HOME}/bin/mvn clean package -Dproject.version=${MAVEN_VERSION}"
        }

        // ถ้า build สำเร็จ ให้เก็บ artifact ไว้
        stage('Archive') {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: false
        }

        // // ตั้งค่าให้ build นี้เป็น success
        // currentBuild.result = 'SUCCESS'

        // // ถ้า build สำเร็จ ให้เก็บ artifact ไว้
        // stage('Archive') {
        //     if currentBuild.result == 'SUCCESS' {
        //         archiveArtifacts artifacts: 'target/*.jar', fingerprint: false
        //     }
        //     else {
        //         echo 'Build is not success, skip archiving'
        //     }
        // }


    } catch (Exception e) {
        // จัดการข้อผิดพลาดถ้ามี
        currentBuild.result = 'FAILURE' // ตั้งค่าให้ build นี้เป็น fail
        throw e
    } finally {
        echo 'Build & Archived SUCCESS!'
    }
}
