
pipeline {
    agent {
        label 'slave-1'
    }

    options {
        // เก็บ 20 build ล่าสุด และ 10 artifact ล่าสุด และเก็บ artifact ไว้ 7 วัน
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10', artifactDaysToKeepStr: '7'))
    }

    stages {
        stage('Clone Repository') {
            steps {
                // checkout code จาก git
                git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/test-ansible'
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                // รัน playbook โดยใช้ไฟล์รหัสผ่าน
                sh 'ansible-playbook -i inventory/hosts.yml playbooks/install-nginx.yml --extra-vars "ansible_become_pass=1234"'
                // ansiblePlaybook playbook: 'playbooks/install_nginx.yml',
                //                 inventory: 'inventory/hosts.yml',
                //                 extras: '-K 1234'
            }
        }
    }

    post {
        success {
            echo 'Ansible playbook ran successfully!'
        }
        failure {
            echo 'Ansible playbook failed.'
        }
    }
}
