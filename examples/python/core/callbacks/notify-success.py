import boto3
import json

lambda_client = boto3.client('lambda')

# When external system succeeds
callback_id = "abc123-callback-id-from-durable-function"
result_data = json.dumps({'status': 'approved', 'amount': 1000}).encode('utf-8')

lambda_client.send_durable_execution_callback_success(
    CallbackId=callback_id,
    Result=result_data
)
