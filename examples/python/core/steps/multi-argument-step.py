@durable_step
def my_step(step_context: StepContext, arg1: str, arg2: int) -> str:
    return f"{arg1}: {arg2}"

result = context.step(my_step("value", 42))
