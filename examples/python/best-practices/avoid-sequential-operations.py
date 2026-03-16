@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Sequential execution - each step waits for the previous one
    user_data = context.step(fetch_user_data(event["user_id"]))
    order_history = context.step(fetch_order_history(event["user_id"]))
    preferences = context.step(fetch_preferences(event["user_id"]))
    
    return {
        "user": user_data,
        "orders": order_history,
        "preferences": preferences,
    }
