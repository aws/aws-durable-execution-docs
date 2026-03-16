from aws_durable_execution_sdk_python.serdes import JsonSerDes
from aws_durable_execution_sdk_python.config import StepConfig

config = StepConfig(serdes=JsonSerDes())
result = context.step(process_json, json_string, config=config)
