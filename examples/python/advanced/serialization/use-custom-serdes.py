from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step, StepContext
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext
import json

class CompressedSerDes(SerDes[dict]):
    """Example: Compress large dictionaries."""
    
    def serialize(self, value: dict, serdes_context: SerDesContext) -> str:
        # In production, use actual compression like gzip
        return json.dumps(value, separators=(',', ':'))
    
    def deserialize(self, data: str, serdes_context: SerDesContext) -> dict:
        return json.loads(data)

@durable_step
def process_large_data(step_context: StepContext, data: dict) -> dict:
    # Process the data
    return {"processed": True, "items": len(data)}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    large_data = {"items": [f"item_{i}" for i in range(1000)]}
    
    # Use custom SerDes for this step
    config = StepConfig(serdes=CompressedSerDes())
    result = context.step(process_large_data(large_data), config=config)
    
    return result
