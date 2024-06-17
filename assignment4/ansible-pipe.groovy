
node('slave-1') {
    // กำหนดตัวเลือกต่างๆ ที่ใช้ใน pipeline
    properties([
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10', artifactDaysToKeepStr: '7'))
    ])

    try {
        stage('Checkout') {
            // checkout code จาก git
            git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/test-ansible'
        }

        stage('check ansible && pwd') {
            // ตรวจสอบว่า ansible ติดตั้งอยู่หรือไม่
            sh 'ansible --version'
            sh 'pwd'
        }

        stage('Ansible plays') {
            // ใช้ ansible ในการทำงาน
            ansiblePlaybook(
                playbook: 'playbooks/install_nginx.yml',
                inventory: 'inventory/hosts.yml',
                extraVars: [
                    ansible_become_pass: '1234'
                ],
                colorized: true // ให้ผลลัพธ์ที่แสดงผลเป็นสี
            ) 
        }

        stage('Test') {
            // ทดสอบการทำงานของ nginx
            sh 'curl 172.17.0.4:80'
        }
    }

    catch (Exception e) {
        // จัดการข้อผิดพลาดถ้ามี
        currentBuild.result = 'FAILURE'
        echo "Ansible Fails" // ตั้งค่าให้ build นี้เป็น fail
        throw e
    } finally {
        echo 'Anisble-Test Done'
    }
}

