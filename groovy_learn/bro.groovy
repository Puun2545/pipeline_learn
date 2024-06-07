#!/usr/bin/env groovy

// GP_NAME_PIPELINE_VERSION has been defined in Jenkins Setting > Global Properties > Environment Variables
// def GP_NAME_PIPELINE_VERSION = AELLA_XPLATFORM_MPOC_PIPELINE_VERSION
// def GP_NAME_PIPELINE_VERSION = "feature/xplatform-15581"
def GP_NAME_PIPELINE_VERSION = "feature/XPLATFORM-15253"
library "jenkins-pipeline-library@${GP_NAME_PIPELINE_VERSION}"

properties([
    buildDiscarder(logRotator(numToKeepStr: '10'))
    , pipelineTriggers([
        GenericTrigger(
          genericVariables: [
            [expressionType: 'JSONPath',key: 'Committer', value: '$.user_name'],
            [expressionType: 'JSONPath',key: 'Branch',    value: '$.ref', regexpFilter: '^refs/heads/'],
            [expressionType: 'JSONPath',key: 'GitRepoHTTPUrl',value: '$.repository.git_http_url'],
            [expressionType: 'JSONPath',key: 'GitRepoName',value: '$.repository.name'],
          ],

          token: "qMkAfKEp3uJYqqJFCPZfZEPeMK76bt9a",
          causeString: '$Committer committed on $Branch',
          
          regexpFilterText: '$GitRepoName: $Branch',
          regexpFilterExpression: '^maven-java17-demo: rc/1.0.x$',
        )
    ]),
    parameters(
        [
            string(defaultValue: '', description: 'e.g. rc/0.1.0', name: 'CandidateBranchName', trim: false),
            string(defaultValue: 'eks_aws-ap-southeast-1-nonbank-nonprod', description: '', name: 'KubernetesClusterName', trim: true)
        ]
    )
])

if (clarifyPipelineTriggerToReloadNewConfig()) return;
multiCloudStdPipelineCommon([
    PipelineTimeout: 60,
    PipelineGroup: "CICD_MS",
    PipelineTemplate: "MS_JRE_MAVEN",
    ArchetypeName: "ms_maven-java17-spring-stdlib_bundle",
    ArchetypeGroup: "microservices",
    CloudProvider: "aws",
    KubernetesClusterName: params.KubernetesClusterName,
    CompanyName: "aella",
    ProjectName: "xplatform-mpoc",
    PackageName: "maven-java17-demo",
    PackageVersion: "",
    PackageDigest: "",
    SourceCodeBranch: "",
    EnvironmentName: "dev",
    GitURL: "https://gitlab.com/scbtechx",
    GitBranch: "main",
    GitProjectMetadataRepo: "https://gitlab.com/scbtechx/aella-xplatform-mpoc/infra-inventory/project-metadata.git", 
    GitJenkinsCredentialId: "xplatform-scbtechx-gitlab",
    RequireLoadServiceInfo: true,
]) { context ->

    multiCloudStageMsJreMavenCICD.envBuildDeploy(context)
    

}