pipeline {
    agent any
    parameters {
        choice(name: 'TOOLS', choices: ['nexus', 'nginx', 'sonarQube'], description: 'Select the deployment TOOLS')
    }
    stages {
        stage('Select Action') {
            steps {
                script {
                    // Define choices based on selected TOOLS
                    def actions = []
                    if (params.TOOLS == 'nexus') {
                        actions = ['install', 'maintain', 'upgrade']
                    } else if (params.TOOLS == 'nginx') {
                        actions = ['install', 'upgrade']
                    } else if (params.TOOLS == 'sonarQube') {
                        actions = ['install', 'maintain', 'update']
                    }

                    // Prompt user for the action based on the selected TOOLS
                    def selectedAction = input message: 'Select an action', parameters: [choice(name: 'ACTION', choices: actions.join('\n'), description: 'Select the action to perform')]

                    // Print the selected parameters
                    echo "Selected TOOL: ${params.TOOLS}"
                    echo "Selected Action: ${selectedAction}"

                    // ech ansible path
                    echo "ansible-playbook -i inventory/hosts.yml playbooks/${params.TOOLS}/${selectedAction}.yml --extra-vars 'ansible_become_pass=1234'"
                }
            }
        }
    }
}
