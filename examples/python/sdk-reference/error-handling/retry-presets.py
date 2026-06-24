from aws_durable_execution_sdk_python.config import Duration, StepConfig
from aws_durable_execution_sdk_python.retries import RetryPresets

# No retries
no_retry_config = StepConfig(retry_strategy=RetryPresets.none())

# Default retries (6 attempts, 5s initial delay, 60s max, 2x backoff)
default_config = StepConfig(retry_strategy=RetryPresets.default())

# Quick retries for transient errors (3 attempts)
transient_config = StepConfig(retry_strategy=RetryPresets.transient())

# Longer retries for resource availability (5 attempts, up to 5 minutes)
resource_config = StepConfig(retry_strategy=RetryPresets.resource_availability())

# Aggressive retries for critical operations (10 attempts)
critical_config = StepConfig(retry_strategy=RetryPresets.critical())

# Linear backoff (6 attempts, delays of 1s, 2s, 3s, 4s, 5s)
linear_config = StepConfig(retry_strategy=RetryPresets.linear())

# Fixed delay (5 attempts, 5 second interval). Pass an interval to customize.
fixed_config = StepConfig(retry_strategy=RetryPresets.fixed())
fixed_2s_config = StepConfig(
    retry_strategy=RetryPresets.fixed(interval=Duration.from_seconds(2))
)
