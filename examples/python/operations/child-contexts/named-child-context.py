from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_with_child_context,
)


@durable_with_child_context
def process_order(ctx: DurableContext, order_id: str) -> str:
    return ctx.step(lambda _: order_id + ":processed", name="process")


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # The name defaults to the function's name when using @durable_with_child_context
    auto_named = context.run_in_child_context(process_order(event["order_id"]))

    # Override the name explicitly
    explicit = context.run_in_child_context(
        process_order(event["order_id"]),
        name="process-order",
    )

    return {"auto_named": auto_named, "explicit": explicit}
