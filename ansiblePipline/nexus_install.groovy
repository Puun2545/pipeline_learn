
node('slave-1') {

    try {
        stage('Checkout Ansible Repository') {
            // checkout code จาก git
            git branch: 'main', credentialsId: 'git-credential', url: 'https://github.com/Puun2545/test-ansible'
        }

        stage('Check Ansible is available') {
            script {
                def ansibleInstalled = sh(script: 'ansible --version', returnStatus: true)
                if (ansibleInstalled != 0) {
                    // ติดตั้ง Ansible ถ้าไม่ได้ติดตั้ง
                    sh 'pip install ansible'
                } else {
                    echo 'Ansible is already installed' 
                }
            }
        }

        stage('Ansible plays') {
            // ใช้ ansible ในการทำงาน
            ansiblePlaybook(
                playbook: 'playbooks/nexus/nexus_install.yml',
                inventory: 'inventory/hosts.yml',
                extraVars: [
                    ansible_become_pass: '1234'
                ],
                colorized: true // ให้ผลลัพธ์ที่แสดงผลเป็นสี
            ) 
        }

    }

    catch (Exception e) {
        // จัดการข้อผิดพลาดถ้ามี
        currentBuild.result = 'FAILURE'
        echo "Ansible Fails" // ตั้งค่าให้ build นี้เป็น fail
        throw e
    } finally {
        echo 'Done Pipeline!'
    }
}

