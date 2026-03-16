from aws_durable_execution_sdk_python.retries import RetryPresets
from aws_durable_execution_sdk_python.config import StepConfig

# No retries
step_config = StepConfig(retry_strategy=RetryPresets.none())

# Default retries (6 attempts, 5s initial delay)
step_config = StepConfig(retry_strategy=RetryPresets.default())

# Quick retries for transient errors (3 attempts)
step_config = StepConfig(retry_strategy=RetryPresets.transient())

# Longer retries for resource availability (5 attempts, up to 5 minutes)
step_config = StepConfig(retry_strategy=RetryPresets.resource_availability())

# Aggressive retries for critical operations (10 attempts)
step_config = StepConfig(retry_strategy=RetryPresets.critical())
