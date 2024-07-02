import boto3
from datetime import datetime
from getParams import get_params

def describe_instances(instance_ids, region):
    ec2_client = boto3.client('ec2', region_name=region)
    
    response = ec2_client.describe_instances(
        InstanceIds=instance_ids,
        Filters=[
            {
                'Name': 'tag:Patch Group',
                'Values': ['*']
            }
        ]
    )
    return response

def create_image(instance_id, region, patch_group):
    ec2_client = boto3.client('ec2', region_name=region)
    
    formatted_date = datetime.now().strftime('%d-%m-%Y')
    image_name = f"{instance_id}-{patch_group}-{formatted_date}"
    
    response = ec2_client.create_image(
        InstanceId=instance_id,
        Name=image_name,
        TagSpecifications=[
            {
                'ResourceType': 'image',
                'Tags': [
                    {
                        'Key': 'Name',
                        'Value': image_name
                    },
                    {
                        'Key': 'Patch Group',
                        'Value': patch_group
                    }
                ]
            }
        ]
    )
    return response['ImageId']

def lambda_handler(event, context):
    instance_list = event['instance_list']
    instance_ids = instance_list['instance_ids']
    _, _, _, _, region, patch_group, _ = get_params(event)
    
    response = describe_instances(instance_ids, region)
    tagged_instances = [inst['InstanceId'] for reservation in response['Reservations'] for inst in reservation['Instances']]
    
    if len(tagged_instances) != len(instance_ids):
        raise Exception("Some instances are not tagged with 'Patch Group'. Please verify.")
    
    image_ids = []
    for instance_id in instance_ids:
        image_id = create_image(instance_id, region, patch_group)
        image_ids.append(image_id)
    
    return {"status": "success", "image_ids": image_ids}
