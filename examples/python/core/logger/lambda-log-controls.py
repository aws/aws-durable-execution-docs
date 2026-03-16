@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    context.logger.debug("This won't appear if log level is INFO or higher")
    context.logger.info("This will appear")
    
    result: str = context.step(
        lambda _: "processed",
        name="process_data",
    )
    
    return result
