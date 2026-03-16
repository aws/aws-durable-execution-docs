@durable_with_child_context
def validate_and_enrich(ctx: DurableContext, data: dict) -> dict:
    validated = ctx.step(validate_data(data))
    enriched = ctx.step(enrich_data(validated))
    return enriched

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    enriched = context.run_in_child_context(
        validate_and_enrich(event["data"]),
        name="validation_phase",
    )
    return enriched
