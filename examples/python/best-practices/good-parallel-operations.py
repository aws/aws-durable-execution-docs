@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Execute all three operations concurrently
    results = context.parallel(
        fetch_user_data(event["user_id"]),
        fetch_order_history(event["user_id"]),
        fetch_preferences(event["user_id"]),
    )
    
    return {
        "user": results[0],
        "orders": results[1],
        "preferences": results[2],
    }
