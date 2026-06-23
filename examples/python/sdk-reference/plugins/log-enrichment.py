import logging

from aws_durable_execution_sdk_python.plugin import DurableInstrumentationPlugin


class ServiceLogFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        record.service = "orders"
        return True


class ExamplePlugin(DurableInstrumentationPlugin):
    def __init__(self) -> None:
        for handler in logging.getLogger().handlers:
            handler.addFilter(ServiceLogFilter())
