@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> list:
    results = []
    for i, item in enumerate(event["items"]):
        result = context.step(
            process_item(item),
            name=f"process_item_{i}_{item['id']}"
        )
        results.append(result)
    return results
