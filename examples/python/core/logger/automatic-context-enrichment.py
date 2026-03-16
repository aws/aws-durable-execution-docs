@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # This log includes: execution_arn
    context.logger.info("Top-level log")
    
    result: str = context.step(
        lambda _: "processed",
        name="process_data",
    )
    
    # This log includes: execution_arn, parent_id, name, attempt
    context.logger.info("Step completed")
    
    return result
