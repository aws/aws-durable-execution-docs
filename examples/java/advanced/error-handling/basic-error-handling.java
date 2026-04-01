try {
    var orderId = (String) input.get("order_id");
    var result = ctx.step("process-order", Map.class,
        stepCtx -> {
            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            return Map.of("order_id", orderId, "status", "processed");
        });
    return result;
} catch (IllegalArgumentException e) {
    // Handle validation errors from your code
    return Map.of("error", "InvalidInput", "message", e.getMessage());
}
