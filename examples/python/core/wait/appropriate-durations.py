# Short wait for rate limiting
context.wait(duration=Duration.from_seconds(30))

# Medium wait for polling intervals
context.wait(duration=Duration.from_minutes(5))

# Long wait for scheduled tasks
context.wait(duration=Duration.from_hours(24))
