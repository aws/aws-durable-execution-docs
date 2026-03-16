@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Explicit name
    result = context.step(
        lambda _: "Step with explicit name",
        name="custom_step"
    )
    return f"Result: {result}"
