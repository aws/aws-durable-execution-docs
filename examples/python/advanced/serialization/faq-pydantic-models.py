order = Order(order_id="ORD-123", amount=99.99)
result = context.step(process_order, order.model_dump())
