callback = context.create_callback(
    name="approval",
    config=CallbackConfig(
        timeout=Duration.from_hours(24),  # Maximum wait time
        heartbeat_timeout=Duration.from_hours(2),  # Fail if no heartbeat for 2 hours
    ),
)
