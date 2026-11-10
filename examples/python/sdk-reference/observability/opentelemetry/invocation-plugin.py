from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python_otel import create_otel_plugin_factory


@durable_execution(plugins=[create_otel_plugin_factory(mode="invocation")])
def handler(event: dict, context: DurableContext) -> str:
    return context.step(lambda ctx: "done", name="process")
