# Good - clear purpose
context.wait(duration=Duration.from_seconds(60), name="rate_limit_cooldown")
context.wait(duration=Duration.from_minutes(5), name="polling_interval")

# Less clear - unnamed waits
context.wait(duration=Duration.from_seconds(60))
context.wait(duration=Duration.from_minutes(5))
