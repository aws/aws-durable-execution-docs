# Break large operation into smaller steps
@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Process in chunks instead of all at once
    items = event["items"]
    chunk_size = 100
    
    results = []
    for i in range(0, len(items), chunk_size):
        chunk = items[i:i + chunk_size]
        result = context.step(
            lambda _, c=chunk: process_chunk(c),
            name=f"process_chunk_{i}"
        )
        results.extend(result)
    
    return {"processed": len(results)}
