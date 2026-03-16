@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    context.step(lambda _: "Starting", name="start")
    context.wait(Duration.from_seconds(3600), name="long_wait")  # 1 hour
    context.step(lambda _: "Continuing", name="continue")
    return "Complete"
