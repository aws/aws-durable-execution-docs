@durable_step
def calculate_result(step_context: StepContext, a: int, b: int) -> int:
    return a + b

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    result = context.step(calculate_result(event["a"], event["b"]))
    return {"result": result}
