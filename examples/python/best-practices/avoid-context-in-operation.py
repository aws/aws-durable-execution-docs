@durable_step
def process_with_wait(step_context: StepContext, context: DurableContext) -> str:
    # DON'T: Can't use context inside its own step operation
    context.wait(Duration.from_seconds(1))  # Error: using context inside step!
    result = context.step(nested_step(), name="step2")  # Error: nested context.step!
    return result

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # This will fail - context is being used inside its own step
    result = context.step(process_with_wait(context), name="step1")
    return {"result": result}
