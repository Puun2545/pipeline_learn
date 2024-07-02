import requests
from getParams import get_params

def get_instance_list_from_gitlab(repo_url, token, file_path, branch='main'):
    headers = {'PRIVATE-TOKEN': token}
    url = f"{repo_url}/repository/files/{file_path}/raw?ref={branch}"
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    return response.json()

def lambda_handler(event, context):
    repo_url, token, file_path, branch, _, _, _ = get_params(event)
    
    instance_list = get_instance_list_from_gitlab(repo_url, token, file_path, branch)
    return {"status": "success", "instance_list": instance_list}
