from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import StepInterruptedError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def long_running_operation(step_context: StepContext) -> dict:
    return {"result": "done"}


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(
            long_running_operation(),
            config=StepConfig(step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY),
        )
        return result
    except StepInterruptedError:
        context.logger.warning("Step interrupted, will not retry")
        raise
