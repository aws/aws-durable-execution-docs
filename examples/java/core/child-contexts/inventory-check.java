var items = (List<Map<String, Object>>) input.get("items");
var orderTotal = ((Number) input.get("total")).doubleValue();

// Check inventory in isolated child context
var inventory = ctx.runInChildContext("inventory-check", Map.class, childCtx -> {
    var results = new ArrayList<Map<String, Object>>();
    for (var item : items) {
        var itemId = (String) item.get("id");
        var available = childCtx.step("check-" + itemId, Boolean.class,
            stepCtx -> checkItemAvailability(item));
        results.add(Map.of("item_id", itemId, "available", available));
    }
    var allAvailable = results.stream().allMatch(r -> (boolean) r.get("available"));
    return Map.of("all_available", allAvailable);
});

if (!(boolean) inventory.get("all_available")) {
    return Map.of("status", "failed", "reason", "items_unavailable");
}

// Process payment in isolated child context
var payment = ctx.runInChildContext("payment-processing", Map.class, childCtx -> {
    var auth = childCtx.step("authorize", Map.class,
        stepCtx -> authorizePayment(orderTotal));
    if ((boolean) auth.get("approved")) {
        var capture = childCtx.step("capture", Map.class,
            stepCtx -> capturePayment((String) auth.get("transaction_id")));
        return Map.of("status", "completed", "transaction_id", capture.get("id"));
    }
    return Map.of("status", "declined");
});

return Map.of("status", payment.get("status"), "transaction_id", payment.get("transaction_id"));
