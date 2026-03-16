# Good - only checkpoint what you need
result = context.step(
    process_data,
    {"id": order.id, "amount": order.amount}
)

# Avoid - large objects in checkpoints
result = context.step(
    process_data,
    entire_database_dump  # Too large
)
