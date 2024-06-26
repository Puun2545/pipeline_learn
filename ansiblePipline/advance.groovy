import groovy.json.JsonSlurper
import groovy.json.JsonOutput

properties ([
    buildDiscarder(logRotator(numToKeepStr: '10')),
    parameters([
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
                                ''' ]))
        ])
])

runPipeline([
    GIT_URL: "https://github.com/Puun2545/test-ansible",
    HOSTS: params.HOSTS,
    TOOLS: params.TOOLS,
    ACTION: params.ACTION,
    JIRA_CARD_ID: params.JIRA_CARD_ID,
    NEXUS_CURRENT_VERSION: params.NEXUS_CURRENT_VERSION
]) { context ->
    echo "Selected TOOL: ${context.TOOLS}"
    echo "Selected HOSTS: ${context.HOSTS}"
    echo "Selected Action: ${context.ACTION}"
    checkOutCode(context)
    loadJSONParams(context)
    checkJSON(context)
    checkAnsiblePath(context)
    runAnsiblePlaybook(context)

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
       git branch: 'main', credentialsId: 'git-credential', url: args.GIT_URL
    }
}

def loadJSONParams(Map args) {
    stage ('Read and Modify JSON') {

        def jsonFile = "./playbooks/${args.TOOLS}/${args.ACTION}Params.json"
        def jsonText = readFile(jsonFile)
        def jsonParams = new JsonSlurper().parseText(jsonText)

        // แปลง LazyMap เป็น HashMap
        def serializableParams = new HashMap(jsonParams)

        def paramsList = []

        serializableParams.parameterDefinitions.each { param ->
            switch (param.type) {
                case 'StringParameterDefinition':
                    paramsList << string(name: param.name, defaultValue: param.defaultValue, description: param.description)
                    break
                case 'BooleanParameterDefinition':
                    paramsList << booleanParam(name: param.name, defaultValue: param.defaultValue, description: param.description)
                    break
                case 'ChoiceParameterDefinition':
                    paramsList << choice(name: param.name, choices: param.choices, description: param.description)
                    break
                case 'PasswordParameterDefinition':
                    paramsList << password(name: param.name, defaultValue: param.defaultValue, description: param.description)
                    break
                case 'FileParameterDefinition':
                    paramsList << file(name: param.name, description: param.description)
                    break
                case 'TextParameterDefinition':
                    paramsList << text(name: param.name, defaultValue: param.defaultValue, description: param.description)
                    break
                case 'RunParameterDefinition':
                    paramsList << run(name: param.name, projectName: param.projectName, description: param.description)
                    break
                case 'CredentialsParameterDefinition':
                    paramsList << credentials(name: param.name, credentialType: param.credentialType, description: param.description)
                    break
            }
        }

        def userInput = input(
            id: 'userInput',
            message: 'กรุณาใส่พารามิเตอร์',
            parameters: paramsList
        )

        // แสดงข้อมูลที่ผู้ใช้กรอก
        userInput.each { key, value ->
            echo "${key}: ${value}"
        }

    }
}

def checkJSON(Map args) {
    stage ('Check JSON') {
        sh "cat ./playbooks/${args.TOOLS}/${args.ACTION}Params.json"
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
        // ใช้ ansible ในการทำงาน
        def ansible_playbook = "playbooks/${args.TOOLS}/${args.TOOLS}_${args.ACTION}.yml"
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
