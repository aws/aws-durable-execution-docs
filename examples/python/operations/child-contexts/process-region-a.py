@durable_with_child_context
def process_region_a(ctx: DurableContext, data: dict) -> dict:
    """Process data for region A."""
    result = ctx.step(lambda _: process_for_region("A", data), name="process_a")
    return {"region": "A", "result": result}

@durable_with_child_context
def process_region_b(ctx: DurableContext, data: dict) -> dict:
    """Process data for region B."""
    result = ctx.step(lambda _: process_for_region("B", data), name="process_b")
    return {"region": "B", "result": result}

@durable_with_child_context
def process_region_c(ctx: DurableContext, data: dict) -> dict:
    """Process data for region C."""
    result = ctx.step(lambda _: process_for_region("C", data), name="process_c")
    return {"region": "C", "result": result}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process data for multiple regions sequentially."""
    data = event["data"]
    
    # Execute child contexts sequentially
    result_a = context.run_in_child_context(
        process_region_a(data),
        name="region_a"
    )
    
    result_b = context.run_in_child_context(
        process_region_b(data),
        name="region_b"
    )
    
    result_c = context.run_in_child_context(
        process_region_c(data),
        name="region_c"
    )
    
    return {
        "regions_processed": 3,
        "results": [result_a, result_b, result_c],
    }
