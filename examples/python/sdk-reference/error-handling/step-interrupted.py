from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import StepInterruptedError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def charge_payment(step_context: StepContext, amount: float) -> dict:
    return charge_external_system(amount)


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(
            charge_payment(event["amount"]),
            config=StepConfig(step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY),
        )
        return {"status": "charged", "result": result}
    except StepInterruptedError:
        # The step started but Lambda was interrupted before the result was
        # checkpointed. The SDK will not re-run the step on the next invocation.
        # Inspect your payment system to determine whether the charge succeeded.
        context.logger.warning("Payment step interrupted — check payment system")
        return {"status": "unknown"}


def charge_external_system(amount: float) -> dict:
    return {"charged": amount}
