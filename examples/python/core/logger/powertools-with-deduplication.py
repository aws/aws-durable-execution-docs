from aws_lambda_powertools import Logger
from aws_durable_execution_sdk_python import DurableContext, durable_execution

logger = Logger(service="order-processing")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Set Powertools for AWS Lambda (Python) on the context
    context.set_logger(logger)
    
    # Now you get BOTH:
    # - Powertools for AWS Lambda (Python) features (JSON logging, correlation IDs, etc.)
    # - Log deduplication during replays
    context.logger.info("Starting workflow")
    
    result1: str = context.step(
        lambda _: "step1-done",
        name="step_1",
    )
    context.logger.info("Step 1 completed", extra={"result": result1})
    
    result2: str = context.step(
        lambda _: "step2-done",
        name="step_2",
    )
    context.logger.info("Step 2 completed", extra={"result": result2})
    
    return f"{result1}-{result2}"
