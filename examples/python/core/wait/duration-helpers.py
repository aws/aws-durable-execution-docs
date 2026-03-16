from aws_durable_execution_sdk_python.config import Duration

# Wait for 30 seconds
context.wait(duration=Duration.from_seconds(30))

# Wait for 5 minutes
context.wait(duration=Duration.from_minutes(5))

# Wait for 2 hours
context.wait(duration=Duration.from_hours(2))

# Wait for 1 day
context.wait(duration=Duration.from_days(1))
