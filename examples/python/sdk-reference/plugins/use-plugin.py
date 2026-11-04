from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.plugin import InvocationStartInfo

from example_plugin import ExamplePlugin


class ExamplePluginFactory:
    def create_plugin(self, info: InvocationStartInfo) -> ExamplePlugin:
        return ExamplePlugin()


@durable_execution(plugins=[ExamplePluginFactory()])
def handler(event: dict, context: DurableContext):
    return context.step(lambda ctx: "done", name="process")
