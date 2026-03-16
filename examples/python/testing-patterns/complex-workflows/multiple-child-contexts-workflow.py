@durable_with_child_context
def validate_data(ctx: DurableContext, data: dict) -> dict:
    return ctx.step(lambda _: {**data, "validated": True}, name="validate")

@durable_with_child_context
def transform_data(ctx: DurableContext, data: dict) -> dict:
    return ctx.step(lambda _: {**data, "transformed": True}, name="transform")

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    data = event["data"]
    
    validated = context.run_in_child_context(validate_data(data), name="validation")
    transformed = context.run_in_child_context(transform_data(validated), name="transformation")
    
    return transformed
