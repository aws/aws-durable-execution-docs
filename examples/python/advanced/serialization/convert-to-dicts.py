# Good - convert to dict first
order_dict = order.to_dict()
result = context.step(process_order, order_dict)

# Avoid - custom objects aren't serializable
result = context.step(process_order, order)  # Will fail
