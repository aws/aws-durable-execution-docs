import boto3
from botocore.config import Config
from aws_durable_execution_sdk_python import durable_execution, DurableContext

# Create a custom boto3 Lambda client with specific configuration
custom_lambda_client = boto3.client(
    'lambda',
    config=Config(
        retries={'max_attempts': 5, 'mode': 'adaptive'},
        connect_timeout=10,
        read_timeout=60,
    )
)

@durable_execution(boto3_client=custom_lambda_client)
def handler(event: dict, context: DurableContext) -> dict:
    # Your durable function logic
    return {"status": "success"}
