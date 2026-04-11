from aws_lambda_powertools import Logger

from aws_durable_execution_sdk_python import DurableContext, durable_execution

powertools_logger = Logger(service="my-service")


@durable_execution
def handler(event: dict, context: DurableContext):
    context.set_logger(powertools_logger)
    context.logger.info("Using Powertools logger")
