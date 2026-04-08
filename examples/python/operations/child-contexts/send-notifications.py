@durable_with_child_context
def send_notifications(ctx: DurableContext, user_id: str, message: str) -> dict:
    """Send notifications through multiple channels."""
    email_sent = ctx.step(
        lambda _: send_email(user_id, message),
        name="send_email"
    )
    
    sms_sent = ctx.step(
        lambda _: send_sms(user_id, message),
        name="send_sms"
    )
    
    push_sent = ctx.step(
        lambda _: send_push_notification(user_id, message),
        name="send_push"
    )
    
    return {
        "email": email_sent,
        "sms": sms_sent,
        "push": push_sent,
    }

@durable_execution
def order_confirmation_handler(event: dict, context: DurableContext) -> dict:
    """Send order confirmation notifications."""
    notifications = context.run_in_child_context(
        send_notifications(
            event["user_id"],
            f"Order {event['order_id']} confirmed"
        ),
        name="order_notifications"
    )
    
    return {"notifications_sent": notifications}

@durable_execution
def shipment_handler(event: dict, context: DurableContext) -> dict:
    """Send shipment notifications."""
    notifications = context.run_in_child_context(
        send_notifications(
            event["user_id"],
            f"Order {event['order_id']} shipped"
        ),
        name="shipment_notifications"
    )
    
    return {"notifications_sent": notifications}
