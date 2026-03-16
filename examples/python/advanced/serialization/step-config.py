from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import StepConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    config = StepConfig(serdes=CustomSerDes())
    result = context.step(my_function(), config=config)
    return result
