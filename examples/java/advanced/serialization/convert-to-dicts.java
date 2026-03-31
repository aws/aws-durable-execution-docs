// Good - convert to Map first
Map<String, Object> orderMap = Map.of(
    "orderId", order.getOrderId(),
    "amount", order.getAmount()
);
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(orderMap));

// Avoid - non-serializable objects without Jackson annotations
var result = ctx.step("process-order", Order.class, stepCtx -> processOrder(order));  // May fail
