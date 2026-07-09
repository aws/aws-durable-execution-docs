# Wrong: the step body mutates an outer list passed by reference.
# Replay returns the cached None without running the body, so the
# list stays empty and the handler returns {"receipts": []} on replay.
@durable_step
def save_and_track(ctx: StepContext, item: dict, target: list) -> None:
    receipt = save_item(item)
    target.append(receipt["id"])


@durable_execution
def handler(event, context: DurableContext) -> dict:
    receipts: list[str] = []
    for item in event["items"]:
        context.step(
            save_and_track(item, receipts),
            name=f"save-{item['id']}",
        )
    return {"receipts": receipts}


# Right: the step returns the receipt id. The handler appends the
# returned value to the outer list, which replay rebuilds from the
# cached step results.
@durable_step
def save_and_return_id(ctx: StepContext, item: dict) -> str:
    receipt = save_item(item)
    return receipt["id"]


@durable_execution
def handler(event, context: DurableContext) -> dict:
    receipts: list[str] = []
    for item in event["items"]:
        receipt_id = context.step(
            save_and_return_id(item),
            name=f"save-{item['id']}",
        )
        receipts.append(receipt_id)
    return {"receipts": receipts}
