@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    a = context.step(lambda _: event["a"])
    b = context.step(lambda _: event["b"])
    sum_val = context.step(lambda _: a + b)
    return {"result": sum_val}
