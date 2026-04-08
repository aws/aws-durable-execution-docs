from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
    durable_with_child_context,
)


@durable_step
def validate(ctx: StepContext, order_id: str, user_id: str) -> dict:
    return {"order_id": order_id, "user_id": user_id}


@durable_step
def charge(ctx: StepContext, order: dict) -> dict:
    return {**order, "charged": True}


# Pass arguments when calling the decorated function
@durable_with_child_context
def process_order(ctx: DurableContext, order_id: str, user_id: str) -> dict:
    validated = ctx.step(validate(order_id, user_id))
    return ctx.step(charge(validated))


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    return context.run_in_child_context(
        process_order(event["order_id"], event["user_id"])
    )
