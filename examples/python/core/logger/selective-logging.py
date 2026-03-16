import logging

# Silence all SDK logs
logging.getLogger("aws_durable_execution_sdk_python").setLevel(logging.WARNING)

# Or silence specific modules only
logging.getLogger("aws_durable_execution_sdk_python.state").setLevel(logging.WARNING)
logging.getLogger("aws_durable_execution_sdk_python.concurrency").setLevel(logging.WARNING)
