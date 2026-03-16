# Pattern: verb_noun for operations
context.step(validate_order(order_id), name="validate_order")
context.step(process_payment(amount), name="process_payment")

# Pattern: noun_action for callbacks
context.create_callback(name="payment_callback")
context.create_callback(name="approval_callback")

# Pattern: descriptive_wait for waits
context.wait(Duration.from_seconds(30), name="payment_confirmation_wait")
