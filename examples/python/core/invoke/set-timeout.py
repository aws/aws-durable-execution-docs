from aws_durable_execution_sdk_python.config import Duration, InvokeConfig

# Timeout after 30 seconds
config = InvokeConfig(timeout=Duration.from_seconds(30))

# Timeout after 5 minutes
config = InvokeConfig(timeout=Duration.from_minutes(5))

# Timeout after 2 hours
config = InvokeConfig(timeout=Duration.from_hours(2))
