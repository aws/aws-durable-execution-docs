@durable_with_child_context
def data_processing(ctx: DurableContext, data: dict) -> dict:
    """Process data in a child context."""
    result = ctx.step(lambda _: transform_data(data), name="transform")
    return result

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Named child context
    result = context.run_in_child_context(
        data_processing(event["data"]),
        name="data_processor"
    )
    return result
