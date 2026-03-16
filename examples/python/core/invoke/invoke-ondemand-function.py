@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Invoke a mix of durable and on-demand functions."""
    user_id = event["user_id"]
    
    # Invoke a regular Lambda function for data fetching
    user_data = context.invoke(
        function_name="fetch-user-data",  # Regular Lambda function
        payload={"user_id": user_id},
        name="fetch_user",
    )
    
    # Invoke a durable function for complex processing
    processed = context.invoke(
        function_name="process-user-workflow",  # Durable function
        payload=user_data,
        name="process_user",
    )
    
    # Invoke another regular Lambda for notifications
    notification = context.invoke(
        function_name="send-notification",  # Regular Lambda function
        payload={"user_id": user_id, "data": processed},
        name="send_notification",
    )
    
    return {
        "status": "completed",
        "notification_sent": notification["sent"],
    }
