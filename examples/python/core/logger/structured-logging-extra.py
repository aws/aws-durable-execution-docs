# Good - structured and queryable
context.logger.info("Order processed", extra={"order_id": order_id, "amount": 100})

# Avoid - harder to query
context.logger.info(f"Order {order_id} processed with amount 100")
