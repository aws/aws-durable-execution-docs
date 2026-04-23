# Wrong: total mutates outside the step, replay restarts it at 0.
@durable_execution
def handler(event, context: DurableContext) -> dict:
    total = 0
    for item in event["items"]:
        context.step(save_item(item), name=f"save-{item['id']}")
        total += item["price"]
    return {"total": total}


# Right: each step returns the new running total.
@durable_step
def save_and_accumulate(ctx: StepContext, item: dict, running_total: float) -> float:
    save_item(item)
    return running_total + item["price"]


@durable_execution
def handler(event, context: DurableContext) -> dict:
    total = 0.0
    for item in event["items"]:
        total = context.step(
            save_and_accumulate(item, total),
            name=f"save-{item['id']}",
        )
    return {"total": total}
