from aws_durable_execution_sdk_python.exceptions import ExecutionError

try:
    result = context.step(process_data, data)
except ExecutionError as e:
    context.logger.error(f"Serialization failed: {e}")
    # Handle error or convert data
