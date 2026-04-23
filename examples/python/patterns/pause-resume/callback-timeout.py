from aws_durable_execution_sdk_python.config import (
    Duration,
    WaitForCallbackConfig,
)

result = context.wait_for_callback(
    lambda callback_id, ctx: approvals_service.request(
        id=event["orderId"], callback_id=callback_id
    ),
    name="wait-for-approval",
    config=WaitForCallbackConfig(timeout=Duration.from_hours(24)),
)
