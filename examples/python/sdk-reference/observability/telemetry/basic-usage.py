from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python_telemetry_exporter import (
    DurableExecutionTelemetryConfig,
    durable_execution_telemetry,
)


@durable_execution(
    plugins=[durable_execution_telemetry(DurableExecutionTelemetryConfig())]
)
def handler(event: dict, context: DurableContext) -> str:
    data = context.step(lambda ctx: event["id"], name="fetch-data")
    context.wait(Duration.from_seconds(5), name="cooldown")
    return context.step(lambda ctx: f"processed {data}", name="process")
