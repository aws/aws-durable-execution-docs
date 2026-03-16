from aws_lambda_powertools import Logger
from aws_durable_execution_sdk_python import DurableContext, durable_execution

logger = Logger(service="order-processing")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # This log appears on every replay
    logger.info("Starting workflow")
    
    result1: str = context.step(
        lambda _: "step1-done",
        name="step_1",
    )
    # This log appears on every replay after step 1
    logger.info("Step 1 completed")
    
    result2: str = context.step(
        lambda _: "step2-done",
        name="step_2",
    )
    # This log appears only once (no more replays after this)
    logger.info("Step 2 completed")
    
    return f"{result1}-{result2}"
