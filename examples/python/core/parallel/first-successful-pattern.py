from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import (
    CompletionConfig,
    ParallelConfig,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Try multiple data sources, use first successful."""
    
    def try_primary_db(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"source": "primary", "data": "..."})
    
    def try_secondary_db(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"source": "secondary", "data": "..."})
    
    def try_cache(ctx: DurableContext) -> dict:
        return ctx.step(lambda _: {"source": "cache", "data": "..."})
    
    # Complete as soon as any source succeeds
    config = ParallelConfig(
        completion_config=CompletionConfig.first_successful()
    )
    
    result: BatchResult[dict] = context.parallel(
        [try_primary_db, try_secondary_db, try_cache],
        config=config,
    )
    
    results = result.get_results()
    if results:
        return results[0]
    
    return {"error": "All sources failed"}
