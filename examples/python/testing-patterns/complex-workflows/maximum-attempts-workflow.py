@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    target = event.get("target", 10)
    state = 0
    attempt = 0
    max_attempts = 5
    
    while attempt < max_attempts and state < target:
        attempt += 1
        state = context.step(lambda _, s=state: s + 1, name=f"attempt_{attempt}")
        
        if state < target:
            context.wait(Duration.from_seconds(1), name=f"wait_{attempt}")
    
    return {"state": state, "attempts": attempt, "reached_target": state >= target}
