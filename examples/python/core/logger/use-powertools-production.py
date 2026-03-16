from aws_lambda_powertools import Logger

logger = Logger(service="my-service")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    context.set_logger(logger)
    # Now you get JSON logs with all Powertools for AWS Lambda (Python) features
    context.logger.info("Processing started")
