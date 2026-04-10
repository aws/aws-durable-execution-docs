from collections.abc import Callable
from aws_durable_execution_sdk_python.retries import RetryDecision

# retry_strategy: Callable[[Exception, int], RetryDecision]
# attempt_count is one-indexed: 1 on the first retry, 2 on the second, etc.
