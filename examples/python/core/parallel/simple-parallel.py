from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process multiple services in parallel."""
    
    def check_inventory(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"service": "inventory", "status": "ok"})
    
    def check_payment(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"service": "payment", "status": "ok"})
    
    def check_shipping(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"service": "shipping", "status": "ok"})
    
    # Execute all checks in parallel
    result: BatchResult[dict] = context.parallel([
        check_inventory,
        check_payment,
        check_shipping,
    ])
    
    return {
        "total": result.total_count,
        "successful": result.success_count,
        "results": result.get_results(),
    }
