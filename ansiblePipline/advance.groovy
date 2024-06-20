properties ([
    buildDiscarder(logRotator(numToKeepStr: '10')),
    parameters([
            string(
                defaultValue: 'unknown',
                description: 'Enter the tools hosts address',
                name: 'HOSTS'
            ),
            activeChoice(
                choiceType: 'PT_SINGLE_SELECT',
                description: 'Select Tools to development',
                filterLength: 1, filterable: false, 
                name: 'TOOLS',
                script: groovyScript(fallbackScript: [classpath: [], oldScript: '', sandbox: false, script: ''], 
                        script: [classpath: [], oldScript: '', sandbox: false, 
                            script: 'return [\' -- SELECT -- \', \'nexus\', \'jenkins\', \'sonarQube\']'])),
            reactiveChoice(
                choiceType: 'PT_SINGLE_SELECT', description: 'Select the actions', filterLength: 1, filterable: false, 
                name: 'ACTION', 
                referencedParameters: 'TOOLS', 
                script: groovyScript(fallbackScript: [classpath: [], oldScript: '', sandbox: false, script: ''], 
                        script: [classpath: [], oldScript: '', sandbox: false, 
                            script: '''
                                if (TOOLS == \'nexus\') { 
                                    return [\'install\', \'upgrade\'] 
                                }
                                else if (TOOLS == \'jenkins\') { 
                                    return [\'upgrade\'] 
                                }
                                else if (TOOLS == \'sonarqube\') {
                                    return [\'upgrade\'] 
                                }
                                else {
                                    return [\'Select TOOLS First\'] 
                                }
                                ''' ])),
            string(
                defaultValue: 'unknown',
                description: 'Enter Tools Current Versions',
                name: 'VERSIONS'
            )
        ])
])

runPipeline([
    GIT_URL: "https://github.com/Puun2545/test-ansible",
    HOSTS: params.HOSTS,
    TOOLS: params.TOOLS,
    ACTION: params.ACTION
]) { context ->
    echo "Selected TOOL: ${context.TOOLS}"
    echo "Selected HOSTS: ${context.HOSTS}"
    echo "Selected Action: ${context.ACTION}"
    checkOutCode(context)
    checkAnsiblePath(context)
    runAnsiblePlaybook(context)

}

def actionInput(String arg) {
    // Action will be selected based on the selected TOOLS
    def actions = []
    if (arg == 'nexus') {
        actions = ['install', 'maintain', 'upgrade']
    } else if (arg == 'nginx') {
        actions = ['install', 'upgrade']
    } else if (arg == 'sonarQube') {
        actions = ['install', 'maintain', 'update']
    }
    def selectedAction = input message: 'Select an action', parameters: [choice(name: 'ACTION', choices: actions.join('\n'), description: 'Select the action to perform')]
    return selectedAction
}

def runPipeline(Map args, Closure stages) {
    node('slave-1') {
        ansiColor('xterm') {
            timestamps(){
                timeout(time: 50, unit: 'MINUTES'){
                    skipDefaultCheckout(true)
                    try {
                        stages(args)
                    } catch (Exception err) {
                        throw err
                    } finally {
                        echo 'DONE'
                    }
                }
            }
        }

    }
}


def checkOutCode(Map args) {
    stage ('Checkout Ansible Repository') {
        // git branch: 'main', credentialsId: 'git-credential', url: ''
        echo "git branch: 'main', credentialsId: 'git-credential', url: '${args.GIT_URL}'"
    }
}

def checkAnsiblePath(Map args) {
    stage ('Check Ansible-playbook run path') {
        echo "ansible-playbook -i inventory/hosts.yml playbooks/${args.TOOLS}/${args.TOOLS}_${args.ACTION}.yml --extra-vars 'ansible_become_pass=1234'"
        sh "pwd"
    }
}

def runAnsiblePlaybook(Map args) {
    stage ('Run Ansible-playbook') {
        def ansible_playbook = "playbooks/${args.TOOLS}/${args.TOOLS}_${args.ACTION}.yml"
        // ใช้ ansible ในการทำงาน
        // sh "ansible-playbook -i inventory/hosts.yml ./${ansible_playbook} --extra-vars 'ansible_become_pass=1234'"
        ansiblePlaybook(
                playbook: ansible_playbook,
                inventory: 'inventory/hosts.yml',
                extraVars: [
                    ansible_become_pass: '1234'
                ],
                colorized: true // ให้ผลลัพธ์ที่แสดงผลเป็นสี
            ) 
    }
}


