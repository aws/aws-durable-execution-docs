from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process an order that requires approval."""
    order_id = event["order_id"]
    
    # Create callback for approval
    approval_callback = context.create_callback(
        name="order_approval",
        config=CallbackConfig(
            timeout=Duration.from_hours(48),  # 48 hours to approve
            heartbeat_timeout=Duration.from_hours(12),  # Check every 12 hours
        ),
    )
    
    # Send approval request to approval system
    # The approval system will use callback.callback_id to respond
    send_to_approval_system({
        "callback_id": approval_callback.callback_id,
        "order_id": order_id,
        "details": event["order_details"],
    })
    
    # Wait for approval
    approval = approval_callback.result()
    
    if approval and approval.get("approved"):
        # Process approved order
        return process_order(order_id)
    else:
        # Handle rejection
        return {"status": "rejected", "reason": approval.get("reason")}
