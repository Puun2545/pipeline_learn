library "my-shared-library@v0.0.1"

testLib()

node('slave-1') {
    // เก็บ 20 build ล่าสุด และ 10 artifact ล่าสุด และเก็บ artifact ไว้ 7 วัน
    properties([
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10', artifactDaysToKeepStr: '7'))
    ])

    // กำหนดเวอร์ชันของ Maven ให้เป็น 0.0.0 ตามด้วยเลข BUILD_NUMBER ที่ได้จาก Jenkins
    def MAVEN_VERSION = "0.0.${env.BUILD_NUMBER}"
    def MAVEN_HOME = tool 'M3' // ใช้ Maven ที่ติดตั้งใน Jenkins

    try {

        stage('Checkout') {
            // ดึงโค้ดจาก GitHub
            git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/java-ci-test'
        }

        stage('Build') {
            // สร้างโปรเจคด้วย Maven
            env.MAVEN_HOME = MAVEN_HOME
            echo "Loading testBuild function..."
            testBuild(MAVEN_VERSION)
        }
    
        stage('Archive') {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: false
        }
    }
    catch (Exception e) {
        // จัดการข้อผิดพลาดถ้ามี
        currentBuild.result = 'FAILURE' // ตั้งค่าให้ build นี้เป็น fail
        echo e.toString()
        throw e
    }
    finally {
        echo 'DONE'
    }
}
