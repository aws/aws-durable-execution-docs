from aws_lambda_powertools import Logger
from aws_durable_execution_sdk_python import DurableContext, durable_execution

logger = Logger(service="order-processing")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Set Powertools for AWS Lambda (Python) on the context
    context.set_logger(logger)
    
    # Now context.logger uses Powertools for AWS Lambda (Python) with automatic enrichment
    context.logger.info("Starting workflow", extra={"event_id": event.get("id")})
    
    result: str = context.step(
        lambda _: "processed",
        name="process_data",
    )
    
    context.logger.info("Workflow completed", extra={"result": result})
    return result
