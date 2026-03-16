# Before - fails
result = context.step(process_order, order_object)

# After - works
result = context.step(process_order, order_object.to_dict())
