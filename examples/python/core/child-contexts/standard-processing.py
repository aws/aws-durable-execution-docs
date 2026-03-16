@durable_with_child_context
def standard_processing(ctx: DurableContext, data: dict) -> dict:
    """Standard data processing."""
    result = ctx.step(lambda _: process_standard(data), name="process")
    return {"type": "standard", "result": result}

@durable_with_child_context
def premium_processing(ctx: DurableContext, data: dict) -> dict:
    """Premium data processing with extra steps."""
    enhanced = ctx.step(lambda _: enhance_data(data), name="enhance")
    validated = ctx.step(lambda _: validate_premium(enhanced), name="validate")
    result = ctx.step(lambda _: process_premium(validated), name="process")
    return {"type": "premium", "result": result}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process data based on customer tier."""
    customer_tier = event.get("tier", "standard")
    
    if customer_tier == "premium":
        result = context.run_in_child_context(
            premium_processing(event["data"]),
            name="premium_processing"
        )
    else:
        result = context.run_in_child_context(
            standard_processing(event["data"]),
            name="standard_processing"
        )
    
    return result
