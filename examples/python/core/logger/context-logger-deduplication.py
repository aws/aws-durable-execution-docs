from aws_durable_execution_sdk_python import DurableContext, durable_execution

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # This log appears only once, even if the function is replayed
    context.logger.info("Starting workflow")
    
    # Step 1 - logs appear only once
    result1: str = context.step(
        lambda _: "step1-done",
        name="step_1",
    )
    context.logger.info("Step 1 completed", extra={"result": result1})
    
    # Step 2 - logs appear only once
    result2: str = context.step(
        lambda _: "step2-done",
        name="step_2",
    )
    context.logger.info("Step 2 completed", extra={"result": result2})
    
    return f"{result1}-{result2}"
