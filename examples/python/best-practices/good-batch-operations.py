@durable_step
def process_batch(step_context: StepContext, items: list) -> list:
    return [process_item(item) for item in items]

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> list:
    items = event["items"]
    results = []
    
    # Process 10 items per step instead of 1
    for i in range(0, len(items), 10):
        batch = items[i:i+10]
        batch_results = context.step(
            process_batch(batch),
            name=f"process_batch_{i//10}"
        )
        results.extend(batch_results)
    
    return results
