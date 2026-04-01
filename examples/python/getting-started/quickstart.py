from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def my_step(step_context: StepContext) -> str:
    step_context.logger.info("Hello from my_step")
    return "Hello from Durable Lambda!"


@durable_execution
def lambda_handler(event, context: DurableContext) -> dict:
    message: str = context.step(my_step())

    # Pause for 10 seconds without consuming CPU or incurring usage charges
    context.wait(Duration.from_seconds(10))

    # Replay-aware: logs once even though the function replays after the wait
    context.logger.info("Resumed after wait")

    return {"statusCode": 200, "body": message}
