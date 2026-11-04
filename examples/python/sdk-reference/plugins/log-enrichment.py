import logging

from aws_durable_execution_sdk_python.plugin import (
    DurableInstrumentationPlugin,
    InvocationStartInfo,
)


class ServiceLogFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        record.service = "orders"
        return True


class ExamplePlugin(DurableInstrumentationPlugin):
    pass


class ExamplePluginFactory:
    def __init__(self) -> None:
        for handler in logging.getLogger().handlers:
            if not any(isinstance(f, ServiceLogFilter) for f in handler.filters):
                handler.addFilter(ServiceLogFilter())

    def create_plugin(self, info: InvocationStartInfo) -> ExamplePlugin:
        return ExamplePlugin()
