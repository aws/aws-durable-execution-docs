from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python_otel import InvocationOtelPluginFactory


@durable_execution(plugins=[InvocationOtelPluginFactory()])
def handler(event: dict, context: DurableContext) -> str:
    return context.step(lambda ctx: "done", name="process")
