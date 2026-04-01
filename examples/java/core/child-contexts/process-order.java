var orderId = (String) input.get("order_id");
var items = (List<?>) input.get("items");

// Execute order processing in a child context
var result = ctx.runInChildContext("process-order", Map.class, childCtx -> {
    // Validate items
    var validation = childCtx.step("validate-items", Map.class,
        stepCtx -> validateItems(items));

    if (!(boolean) validation.get("valid")) {
        return Map.of("status", "invalid", "errors", validation.get("errors"));
    }

    // Calculate total
    var total = childCtx.step("calculate-total", Double.class,
        stepCtx -> calculateTotal(items));

    // Process payment
    var payment = childCtx.step("process-payment", Map.class,
        stepCtx -> processPayment(orderId, total));

    return Map.of(
        "order_id", orderId,
        "total", total,
        "payment_status", payment.get("status"));
});

return result;
