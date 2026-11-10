import logging

from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python_otel import create_otel_plugin_factory


root_logger = logging.getLogger()
root_logger.setLevel(logging.INFO)
formatter = logging.Formatter(
    "%(message)s traceId=%(traceId)s spanId=%(spanId)s "
    "otelTraceSampled=%(otelTraceSampled)s",
    defaults={"traceId": "-", "spanId": "-", "otelTraceSampled": "-"},
)
for log_handler in root_logger.handlers:
    log_handler.setFormatter(formatter)

root_logger.info("Function initialized")


@durable_execution(plugins=[create_otel_plugin_factory()])
def handler(event: dict, context: DurableContext) -> str:
    context.logger.info("Processing request")
    return "done"
