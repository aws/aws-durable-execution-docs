@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> list:
    results = []
    # Creating a step for each item - too many checkpoints!
    for i, item in enumerate(event["items"]):
        result = context.step(
            lambda _, item=item: process_item(item),
            name=f"process_item_{i}"
        )
        results.append(result)
    return results
