from aws_lambda_powertools import Logger
from aws_durable_execution_sdk_python import DurableContext, durable_execution

logger = Logger(service="order-processing")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    logger.info("Starting workflow")
    
    result: str = context.step(
        lambda _: "processed",
        name="process_data",
    )
    
    logger.info("Workflow completed", extra={"result": result})
    return result
