import java.text.SimpleDateFormat
def instancesPatchGroup = [:]
pipeline {
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
            choices: ["aella-nonbank-nonprod","aella-devops","aella-bank-nonprod","aella-sandbox","aella-network-prod"], 
            description: ''
        )
        text(
            name: 'INSATANCES', 
            defaultValue: '', 
            description: 'Please add insance id sperate by newline'
        )
        text(
            name: "EXECUTION_TIMEOUT_SECONDS",
            defaultValue: '1200',
            description: 'Default timeout to wait excution default(20mins)'
        )
        booleanParam(
            name: 'BACKUP',
            defaultValue: false,
            description: 'Enable for create image of instances as backup'
        )
    }
    stages{
        stage("Check Patch Group tag"){
            steps{
                script{
                    instanceList = params.INSATANCES.split("\n")
                    instances = ""
                    insanceCount = instanceList.size()
                    instanceList.eachWithIndex{ instance,index ->
                        instances +=instance + " "
                    }
                    RoleARN = libAWSAccountInfo(awsAccount: params.ACCOUNT)["awsProviderRoleARN"]
                    checkInstanceList = readJSON text: sh(returnStdout: true, 
                                            script: """
                                            set +x
                                            export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                                            \$(aws sts assume-role \
                                            --role-arn ${RoleARN} \
                                            --role-session-name ${params.ACCOUNT} \
                                            --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                                            --output text))
                                            set -x
                                            aws ec2  describe-instances \
                                            --region ${env.DEFAULT_REGION} \
                                            --instance-ids ${instances} \
                                            --filters Name="tag-key",Values="Patch Group" \
                                            --query "Reservations[*].Instances[*].[InstanceId,Tags[?Key=='Patch Group']]" \
                                            --output json
                                        """)
                    if(insanceCount != checkInstanceList.size()){
                       error "Some Instances not tagged patch group please kindly verify"
                    }
                    checkInstanceList.eachWithIndex{instance,index ->
                        instanceId=instance[0][0]
                        patchGroup=instance[0][1][0].Value
                        instancesPatchGroup["${instanceId}"]=patchGroup
                    }
                }
            }
        }
        stage("Create image as Backup"){
            when {
                expression {
                    return params.BACKUP
                }
            }
            steps{
                script{
                    RoleARN = libAWSAccountInfo(awsAccount: params.ACCOUNT)["awsProviderRoleARN"]
                    instanceList = params.INSATANCES.split("\n")
                    imageIds = ""
                    format = new SimpleDateFormat("dd-MM-YYYY")
                    date = new Date()
                    formattedDate = format.format(date)
                    // BUILD_TRIGGER_BY = "${currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')[0]['userId']}"
                    wrap([$class: 'BuildUser']) {
                        BUILD_TRIGGER_BY = BUILD_USER
                    }
                    instanceList.eachWithIndex{ instance,index ->
                        existingBackup = readJSON text: sh(returnStdout: true,
                        script:"""
                        set +x
                        export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                        \$(aws sts assume-role \
                        --role-arn ${RoleARN} \
                        --role-session-name ${params.ACCOUNT} \
                        --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                        --output text))
                        set -x
                        aws ec2 describe-images --filters 'Name=name,Values=${instance}-${patchGroup}-${formattedDate}'
                        """
                        ) 
                        if(existingBackup.Images.size()==0){
                            patchGroup=instancesPatchGroup["${instance}"]
                            imageOutput = readJSON text: sh(returnStdout: true,
                            script:"""
                            set +x
                            export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                            \$(aws sts assume-role \
                            --role-arn ${RoleARN} \
                            --role-session-name ${params.ACCOUNT} \
                            --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                            --output text))
                            set -x
                            aws ec2 create-image --instance-id ${instance} --name ${instance}-${patchGroup}-${formattedDate} \
                            --tag-specifications 'ResourceType=image,Tags=[{Key=Name,Value=${instance}-${patchGroup}-${formattedDate}},{Key="Patch Group",Value=${patchGroup}},{Key=AdminName,Value=${BUILD_TRIGGER_BY}}]'
                            """)
                            imageIds += imageOutput.ImageId+" "
                        }
                    }
                    if(imageIds!=""){
                    sh"""
                    set +x
                        export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                        \$(aws sts assume-role \
                        --role-arn ${RoleARN} \
                        --role-session-name ${params.ACCOUNT} \
                        --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                        --output text))
                    set -x
                    aws ec2 wait image-available --image-ids ${imageIds} --cli-read-timeout 1800 --cli-connect-timeout 1800
                    """
                    }
                }
            }
        }
        stage("Execute Patch"){
            steps{
                script{
                    instanceList = params.INSATANCES.split("\n")
                    ssmInstances = ""
                    instanceList.eachWithIndex{ instance,index ->
                        ssmInstances +=instance
                        if(index != instanceList.size()-1)
                            ssmInstances +=","
                    }
                    RoleARN = libAWSAccountInfo(awsAccount: params.ACCOUNT)["awsProviderRoleARN"]
                    def automationExecutionId = readJSON text: sh(returnStdout: true,
                    script:"""
                        set +x
                        export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                        \$(aws sts assume-role \
                        --role-arn ${RoleARN} \
                        --role-session-name ${params.ACCOUNT} \
                        --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                        --output text))
                        set -x
                        aws ssm start-automation-execution \
                        --region ${env.DEFAULT_REGION} \
                        --document-name "AWS-PatchInstanceWithRollback" \
                        --target-parameter-name InstanceId \
                        --targets Key=ParameterValues,Values=${ssmInstances}
                    """)
                    env.executionId=automationExecutionId.AutomationExecutionId
                    timeout_seconds=params.EXECUTION_TIMEOUT_SECONDS.toInteger()
                    start_time=0
                    while(true){
                        if(start_time>=timeout_seconds){
                            error "Timeout reached (${timeout_seconds} seconds)"
                            break
                        }
                        // status=readJSON text: sh(returnStdout: true,
                        // script:"aws ssm describe-automation-executions --filters \"Key=ExecutionId,Values=${automationExecutionId}\" --output json | jq -r '.AutomationExecutionMetadataList[].AutomationExecutionStatus'"
                        // )
                        status= sh(returnStdout: true,
                        script:"""
                        set +x
                        export \$(printf "AWS_ACCESS_KEY_ID=%s AWS_SECRET_ACCESS_KEY=%s AWS_SESSION_TOKEN=%s" \
                        \$(aws sts assume-role \
                        --role-arn ${RoleARN} \
                        --role-session-name ${params.ACCOUNT} \
                        --query "Credentials.[AccessKeyId,SecretAccessKey,SessionToken]" \
                        --output text))
                        set -x
                        aws ssm describe-automation-executions --filters \"Key=ExecutionId,Values=${automationExecutionId.AutomationExecutionId}\" --output json | jq -r '.AutomationExecutionMetadataList[].AutomationExecutionStatus'
                        """
                        ).trim()
                        if(status=="Success"){
                            env.status=status
                            break
                        }else if (status!="InProgress"){
                            env.status=status
                            error "execution failed"
                            break
                        }
                        else{
                            println "Status is not Success. Waiting..."
                            start_time=start_time+60
                            sleep(60)
                        }
                    }
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