pipeline{
    agent {node "terraform"}
    environment{
        DEFAULT_REGION="ap-southeast-1"
    }
    libraries {
        lib('jenkins-pipeline-library')
    }
    parameters{
        choice(
            name: 'ACCOUNT', 
            choices: ["aella-nonbank-nonprod","aella-devops","aella-bank-nonprod","aella-sandbox", "aella-nonbank-prod"], 
            description: ''
        )
        text(
            name: 'INSATANCES', 
            defaultValue: '', 
            description: 'Please add insance id sperate by newline'
        )
        string(
            name: 'PATCH_GROUP',
            defaultValue: '',
            description: 'Patch group will be tagged to instacnes'
        )
    }
    stages{
        stage('Prepare Job') {
            steps {
                script {
                    currentBuild.description = "AWS Account Name: ${params.ACCOUNT}"
                }
            }
        }
        stage("Patch tag group"){
            steps{
                script{
                    instanceList = params.INSATANCES.split("\n")
                    insances = ""
                    for(instace in instanceList) {
                        insances +=instace + " "
                    }
                    RoleARN = libAWSAccountInfo(awsAccount: params.ACCOUNT)["awsProviderRoleARN"]
                    // println(insances)
                    sh"""
                        set +x
                        export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                        \$(aws sts assume-role \
                        --role-arn ${RoleARN} \
                        --role-session-name ${params.ACCOUNT} \
                        --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                        --output text))
                        set -x
                        aws ec2 create-tags \
                            --region ${env.DEFAULT_REGION} \
                            --resources ${insances} \
                            --tags Key="Patch Group",Value=${params.PATCH_GROUP}
                    """
                }
                
            }
        }
    }
    post {
        always {
            deleteDir()
        }
        success {
            echo 'Job Success'
        }
        unstable {
            echo 'Job Unstable'
        }
        failure {
            echo 'Job Failure'
        }
    }
}