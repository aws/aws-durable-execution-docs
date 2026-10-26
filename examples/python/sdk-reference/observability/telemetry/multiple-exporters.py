from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python_telemetry_exporter import (
    DurableExecutionTelemetryConfig,
    DynamoDBExporter,
    EventBridgeExporter,
    S3Exporter,
    durable_execution_telemetry,
)

telemetry = durable_execution_telemetry(
    DurableExecutionTelemetryConfig(
        emit_mode="on-complete",
        sampling_rate=1.0,
        exporters=[
            S3Exporter(bucket="durable-execution-telemetry-archive"),
            DynamoDBExporter(table_name="durable-execution-telemetry-live"),
            EventBridgeExporter(),
        ],
    )
)


@durable_execution(plugins=[telemetry])
def handler(event: dict, context: DurableContext) -> str:
    data = context.step(lambda ctx: event["id"], name="fetch-data")
    context.wait(Duration.from_seconds(5), name="cooldown")
    return context.step(lambda ctx: f"processed {data}", name="process")
