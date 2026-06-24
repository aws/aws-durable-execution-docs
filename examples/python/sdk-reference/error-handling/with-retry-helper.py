from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.context import DurableContext
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    WithRetryConfig,
    create_retry_strategy,
    with_retry,
)


def charge_flow(ctx: DurableContext, attempt: int) -> str:
    # invoke does not accept a retry strategy, so with_retry handles backoff.
    return ctx.invoke(
        function_name="process-payment",
        payload={"order_id": "abc"},
        name=f"charge-{attempt}",
    )


retry_config = WithRetryConfig(
    retry_strategy=create_retry_strategy(
        RetryStrategyConfig(
            max_attempts=3,
            initial_delay=Duration.from_seconds(2),
            backoff_rate=2.0,
        )
    ),
)


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return with_retry(
        context,
        func=charge_flow,
        config=retry_config,
        name="charge-payment",
    )
