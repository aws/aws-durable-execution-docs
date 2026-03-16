# Avoid - too short, will raise ValidationError
context.wait(duration=Duration.from_seconds(0))

# Minimum - 1 second
context.wait(duration=Duration.from_seconds(1))

# Better - use meaningful durations
context.wait(duration=Duration.from_seconds(5))
