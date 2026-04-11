from aws_durable_execution_sdk_python import DurableContext, durable_execution


@durable_execution
def handler(event: dict, context: DurableContext):
    # context.logger suppresses duplicate logs during replay by default.
    # Logs from completed operations do not repeat when the SDK replays.
    context.logger.info("Step 1 starting")

    result = context.step(lambda ctx: "result")

    context.logger.info("Step 1 complete", extra={"result": result})
    return result
