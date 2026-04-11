from aws_durable_execution_sdk_python import DurableContext, StepContext, durable_execution


@durable_execution
def handler(event: dict, context: DurableContext):
    def process(step_ctx: StepContext) -> str:
        # step_ctx.logger includes executionArn, operationId, and attempt.
        step_ctx.logger.info("Running step")
        return "done"

    return context.step(process, name="process")
