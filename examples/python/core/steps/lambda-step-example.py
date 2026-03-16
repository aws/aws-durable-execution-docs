# With @durable_step (recommended)
result = context.step(validate_order(order_id))

# Optionally, use a lambda function
result = context.step(lambda _: validate_order_logic(order_id))
