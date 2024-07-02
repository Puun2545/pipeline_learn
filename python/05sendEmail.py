import boto3

def send_email(subject, body, to_addresses, from_address):
    ses_client = boto3.client('ses')
    response = ses_client.send_email(
        Source=from_address,
        Destination={
            'ToAddresses': to_addresses
        },
        Message={
            'Subject': {
                'Data': subject
            },
            'Body': {
                'Text': {
                    'Data': body
                }
            }
        }
    )
    return response

def lambda_handler(event, context):
    subject = "Patching Notification"
    from_address = "sender@example.com"
    
    cab_committee_addresses = ["cab1@example.com", "cab2@example.com"]
    xplatform_customer_addresses = ["customer1@example.com", "customer2@example.com"]
    platform_team_addresses = ["team1@example.com", "team2@example.com"]
    
    instance_list = event['instance_list']
    instance_list_str = "\n".join(instance_list)
    
    body = f"This is a notification for the upcoming patching. The following instances will be patched:\n\n{instance_list_str}"
    
    send_email(subject, body, cab_committee_addresses, from_address)
    send_email(subject, body, xplatform_customer_addresses, from_address)
    send_email(subject, body, platform_team_addresses, from_address)
    
    return {"status": "success"}
