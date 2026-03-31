// BusinessLogic.java
static Map<String, Object> validateOrder(Map<String, Object> order) {
    if (!order.containsKey("items") || ((List<?>) order.get("items")).isEmpty()) {
        throw new IllegalArgumentException("Order must have items");
    }
    var result = new HashMap<>(order);
    result.put("validated", true);
    return result;
}

// Handler.java
var order = (Map<String, Object>) input.get("order");
var validatedOrder = ctx.step("validate_order", Map.class,
    stepCtx -> validateOrder(order));
return Map.of("status", "completed", "order_id", validatedOrder.get("order_id"));
