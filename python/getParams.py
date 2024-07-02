def get_params(event):
    repo_url = event['repo_url'] # GitLab repository URL
    token = event['token'] # GitLab private token
    file_path = event['file_path'] # Path to the file containing the instance list
    branch = event.get('branch', 'main') # Branch name

    ''' ควรอยู่ใน JSON ที่ส่งเข้ามารึป่าว '''
    region = event['region'] # AWS region
    patch_group = event.get('patch_group') # Patch group name

    timeout_seconds = int(event.get('timeout_seconds', 1200)) # Timeout in seconds
    
    return repo_url, token, file_path, branch, region, patch_group, timeout_seconds


