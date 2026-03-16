@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    state = 0
    attempt = 0
    max_attempts = 5
    
    while attempt < max_attempts:
        attempt += 1
        
        state = context.step(lambda _, s=state: s + 1, name=f"increment_{attempt}")
        
        if state >= 3:
            break
        
        context.wait(Duration.from_seconds(1), name=f"wait_{attempt}")
    
    return state
