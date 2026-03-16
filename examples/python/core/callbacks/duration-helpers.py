from aws_durable_execution_sdk_python.config import Duration

# Various ways to specify duration
timeout_60s = Duration.from_seconds(60)
timeout_5m = Duration.from_minutes(5)
timeout_2h = Duration.from_hours(2)
timeout_1d = Duration.from_days(1)

# Use in CallbackConfig
config = CallbackConfig(
    timeout=Duration.from_hours(2),
    heartbeat_timeout=Duration.from_minutes(15),
)
