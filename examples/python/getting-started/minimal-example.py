from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)

@durable_step
def greet_user(step_context: StepContext, name: str) -> str:
    """Generate a greeting."""
    return f"Hello {name}!"

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Simple durable function."""
    name = event.get("name", "World")
    greeting = context.step(greet_user(name))
    return greeting
