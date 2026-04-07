from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import InvokeConfig


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    config = InvokeConfig(tenant_id=event.get("tenant_id"))

    result = context.invoke(
        "order-processor-function:live",
        {"order_id": event["order_id"]},
        name="process-order",
        config=config,
    )

    return result
