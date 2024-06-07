pipeline {
    agent any

    environment {
        MY_CREDENTIAL = credentials('git-credential')
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
                sh "${env.MAVEN_HOME}/bin/mvn clean package"
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
