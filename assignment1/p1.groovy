pipeline {
    agent none
    
    stages {
        stage('Build on Agent 01') {
            agent { 
                label 'slave-1' 
            }
            steps {
                script {
                    sh 'touch file01.txt'
                    echo 'Hi01'
                }
            }
        }
        stage('Build on Agent 02') {
            agent { 
                label 'slave-2' 
            }
            steps {
                script {
                    sh 'touch file02.txt'
                    echo 'Hi02'
                }
            }
        }
    }
}
