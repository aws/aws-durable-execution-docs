@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    data = {"count": 0}
    context.step(increment_count(data))
    data["count"] += 1  # DON'T: Mutation outside step
    return data
