from aws_durable_execution_sdk_python.config import CallbackConfig

# Increase timeout
callback = context.create_callback(
    config=CallbackConfig(
        timeout_seconds=7200,  # 2 hours
        heartbeat_timeout_seconds=300,  # 5 minutes
    ),
    name="long_running_approval"
)
