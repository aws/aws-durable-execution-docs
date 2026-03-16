from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Nested parallel execution."""
    
    def process_group_a(ctx: DurableContext) -> list:
        # Inner parallel operation for group A
        task1 = lambda c: c.step(lambda _: "group-a-item-1")
        task2 = lambda c: c.step(lambda _: "group-a-item-2")
        task3 = lambda c: c.step(lambda _: "group-a-item-3")
        
        inner_result = ctx.parallel([task1, task2, task3])
        return inner_result.get_results()
    
    def process_group_b(ctx: DurableContext) -> list:
        # Inner parallel operation for group B
        task1 = lambda c: c.step(lambda _: "group-b-item-1")
        task2 = lambda c: c.step(lambda _: "group-b-item-2")
        task3 = lambda c: c.step(lambda _: "group-b-item-3")
        
        inner_result = ctx.parallel([task1, task2, task3])
        return inner_result.get_results()
    
    # Outer parallel operation
    result: BatchResult[list] = context.parallel([process_group_a, process_group_b])
    
    return {
        "groups_processed": result.success_count,
        "results": result.get_results(),
    }
