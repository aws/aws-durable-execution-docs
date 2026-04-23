from aws_durable_execution_sdk_python.config import Duration

context.wait(Duration.from_hours(24), name="cool-off")
