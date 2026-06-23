from aws_durable_execution_sdk_python import DurableContext, durable_execution

from example_plugin import ExamplePlugin


@durable_execution(plugins=[ExamplePlugin()])
def handler(event: dict, context: DurableContext):
    return context.step(lambda ctx: "done", name="process")
