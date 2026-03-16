@durable_execution
def handler(event: dict, context: DurableContext) -> list[int]:
    numbers = event.get("numbers", [1, 2, 3, 4, 5])
    
    results = []
    for i, num in enumerate(numbers):
        result = context.step(lambda _, n=num: n * 2, name=f"square_{i}")
        results.append(result)
    
    return results
