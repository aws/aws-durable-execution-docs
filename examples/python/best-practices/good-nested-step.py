@durable_step
def nested_step(step_context: StepContext) -> str:
    return "nested step"

@durable_with_child_context
def process_with_wait(child_ctx: DurableContext) -> str:
    # Use child context for nested operations
    child_ctx.wait(seconds=1)
    result = child_ctx.step(nested_step(), name="step2")
    return result

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Use run_in_child_context for nested operations
    result = context.run_in_child_context(
        process_with_wait(),
        name="block1"
    )
    return {"result": result}
