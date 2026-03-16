from aws_durable_execution_sdk_python import BatchResult
from aws_durable_execution_sdk_python.config import ParallelConfig

# Process 100 items, but only 10 at a time
config = ParallelConfig(max_concurrency=10)
result: BatchResult = context.parallel(functions, config=config)
