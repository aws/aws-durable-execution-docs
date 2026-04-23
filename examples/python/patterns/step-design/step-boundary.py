# Wrong: calling context.step inside a step callback is invalid.
@durable_step
def outer(ctx: StepContext, context: DurableContext) -> None:
    context.step(work())  # ERROR: ctx is a StepContext, not a DurableContext


# Right: group durable operations in a child context.
def order_pipeline(child: DurableContext) -> str:
    child.step(validate())
    child.step(charge())
    return "done"


context.run_in_child_context(order_pipeline, name="order-pipeline")
