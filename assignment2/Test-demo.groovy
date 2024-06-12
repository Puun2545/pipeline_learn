node ('slave-1'){ // กำหนด agent ที่จะใช้ในการ build ตรงนี้
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
            // ใช้ Maven ที่ติดตั้งใน Jenkins -D คือการส่งค่าเข้าไปใน pom.xml ที่ project.version นั้นเอง แบบ Dynamics 
            sh "${MAVEN_HOME}/bin/mvn clean package -Dproject.version=${MAVEN_VERSION}"
        }

        stage('Test') {
            sh "${MAVEN_HOME}/bin/mvn test"
        }

        // ถ้า build สำเร็จ ให้เก็บ artifact ไว้
        stage('Archive') {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: false
        }

    } catch (Exception e) {
        // จัดการข้อผิดพลาดถ้ามี
        currentBuild.result = 'FAILURE' // ตั้งค่าให้ build นี้เป็น fail
        throw e
    } finally {
        echo 'DONE'
    }
}
