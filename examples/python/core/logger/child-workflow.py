from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_with_child_context,
)

@durable_with_child_context
def child_workflow(ctx: DurableContext) -> str:
    # Logger includes parent_id for the child context
    ctx.logger.info("Running in child context")
    
    # Step in child context has nested parent_id
    child_result: str = ctx.step(
        lambda _: "child-processed",
        name="child_step",
    )
    
    ctx.logger.info("Child workflow completed", extra={"result": child_result})
    return child_result

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Top-level logger: only execution_arn
    context.logger.info("Starting workflow", extra={"event_id": event.get("id")})
    
    # Child context inherits logger and adds its own parent_id
    result: str = context.run_in_child_context(
        child_workflow(),
        name="child_workflow"
    )
    
    context.logger.info("Workflow completed", extra={"result": result})
    return result
