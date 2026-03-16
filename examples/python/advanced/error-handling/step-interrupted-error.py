from aws_durable_execution_sdk_python import StepInterruptedError

# This can happen if Lambda times out during step execution
@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(long_running_operation())
        return result
    except StepInterruptedError as e:
        # Step was interrupted, will retry on next invocation
        context.logger.warning(f"Step interrupted: {e.step_id}")
        raise
