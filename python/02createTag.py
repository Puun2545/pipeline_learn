import boto3
from getParams import get_params

def create_tags(instance_ids, region, patch_group):
    ec2_client = boto3.client('ec2', region_name=region)
    
    # Create tags for the instances
    ec2_client.create_tags( 
        Resources=instance_ids,
        Tags=[
            {
                'Key': 'Patch Group',
                'Value': patch_group
            }
        ]
    )

def lambda_handler(event, context):
    instance_list = event['instance_list'] # instance_list is a dictionary from the previous step
    instance_ids = instance_list['instance_ids']
    _, _, _, _, region, patch_group, _ = get_params(event)
    
    create_tags(instance_ids, region, patch_group)
    return {"status": "success", "instance_ids": instance_ids}
