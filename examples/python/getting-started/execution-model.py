@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Step 1: Call external API
    data = context.step(fetch_data(event["id"]))
    
    # Step 2: Wait 30 seconds
    context.wait(Duration.from_seconds(30))
    
    # Step 3: Process the data
    result = context.step(process_data(data))
    
    return result
