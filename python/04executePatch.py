import boto3
import time
from getParams import get_params

def start_automation_execution(instance_ids, region):
    ssm_client = boto3.client('ssm', region_name=region)
    
    response = ssm_client.start_automation_execution(
        DocumentName='AWS-PatchInstanceWithRollback',
        TargetParameterName='InstanceId',
        Targets=[
            {
                'Key': 'InstanceIds',
                'Values': instance_ids
            }
        ]
    )
    return response['AutomationExecutionId']

def check_automation_execution_status(execution_id, region, timeout_seconds=1200):
    ssm_client = boto3.client('ssm', region_name=region)
    
    start_time = time.time()
    while True:
        response = ssm_client.describe_automation_executions(
            Filters=[
                {
                    'Key': 'ExecutionId',
                    'Values': [execution_id]
                }
            ]
        )
        status = response['AutomationExecutionMetadataList'][0]['AutomationExecutionStatus']
        
        if status == 'Success':
            return status
        elif status not in ['InProgress', 'Pending']:
            raise Exception(f"Automation execution failed with status: {status}")
        
        if time.time() - start_time > timeout_seconds:
            raise Exception(f"Timeout reached ({timeout_seconds} seconds)")
        
        time.sleep(60)

def lambda_handler(event, context):
    instance_list = event['instance_list']
    instance_ids = instance_list['instance_ids']
    _, _, _, _, region, _, timeout_seconds = get_params(event)
    
    execution_id = start_automation_execution(instance_ids, region)
    status = check_automation_execution_status(execution_id, region, timeout_seconds)
    
    return {"status": status, "execution_id": execution_id}
