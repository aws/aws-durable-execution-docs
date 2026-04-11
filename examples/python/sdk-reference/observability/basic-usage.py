from aws_durable_execution_sdk_python import DurableContext, durable_execution


@durable_execution
def handler(event: dict, context: DurableContext):
    context.logger.info("Starting workflow")

    result = context.step(lambda ctx: "done")

    context.logger.info("Workflow complete", extra={"result": result})
    return result
