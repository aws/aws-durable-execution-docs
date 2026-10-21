from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python_telemetry_exporter import (
    ContentConfig,
    DurableExecutionTelemetryConfig,
    durable_execution_telemetry,
)

telemetry = durable_execution_telemetry(
    DurableExecutionTelemetryConfig(
        sampling_rate=0.1,
        content=ContentConfig(
            input=lambda data: {"orderId": data.get("orderId")},
            output=lambda result: {"status": result.get("status")},
        ),
    )
)


@durable_execution(plugins=[telemetry])
def handler(event: dict, context: DurableContext) -> dict:
    order = context.step(lambda ctx: event["orderId"], name="fetch-order")
    context.wait(Duration.from_seconds(5), name="cooldown")
    return context.step(
        lambda ctx: {"orderId": order, "status": "processed"}, name="process"
    )
