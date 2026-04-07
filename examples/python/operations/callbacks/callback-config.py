from typing import Any

from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration


@durable_execution
def handler(event: Any, context: DurableContext) -> dict:
    config = CallbackConfig(
        timeout=Duration.from_hours(24),
        heartbeat_timeout=Duration.from_minutes(30),
    )
    callback = context.create_callback(name="wait-for-payment", config=config)

    submit_payment_request(callback.callback_id, event["amount"])
    return callback.result()
