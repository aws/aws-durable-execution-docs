@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    data = {"count": 0}
    data = context.step(increment_count(data))
    data = context.step(increment_count(data))
    return data
