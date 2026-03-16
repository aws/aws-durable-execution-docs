from aws_durable_execution_sdk_python import DurableContext, durable_execution

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Log at the top level
    context.logger.info("Starting workflow", extra={"event_id": event.get("id")})
    
    # Execute a step
    result: str = context.step(
        lambda _: "processed",
        name="process_data",
    )
    
    context.logger.info("Workflow completed", extra={"result": result})
    return result
