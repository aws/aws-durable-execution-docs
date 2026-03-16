import base64
import json

from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext


class EncryptedSerDes(SerDes[dict]):
    """Example: Encrypt sensitive data (simplified for demonstration)."""

    def __init__(self, encryption_key: str):
        self.encryption_key = encryption_key

    def serialize(self, value: dict, serdes_context: SerDesContext) -> str:
        json_str = json.dumps(value)
        # In production, use proper encryption like AWS KMS
        encrypted = base64.b64encode(json_str.encode()).decode()
        return encrypted

    def deserialize(self, data: str, serdes_context: SerDesContext) -> dict:
        # In production, use proper decryption
        decrypted = base64.b64decode(data.encode()).decode()
        return json.loads(decrypted)


@durable_step
def process_sensitive_data(step_context: StepContext, data: dict) -> dict:
    return {"processed": True}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    sensitive_data = {"ssn": "123-45-6789", "credit_card": "4111-1111-1111-1111"}

    # Encrypt data in checkpoints
    config = StepConfig(serdes=EncryptedSerDes("my-key"))
    result = context.step(process_sensitive_data(sensitive_data), config=config)

    return result
