// Execute all checks concurrently using async child contexts
var inventoryFuture = ctx.runInChildContextAsync("check-inventory", Map.class,
    child -> child.step("check", Map.class,
        stepCtx -> Map.of("service", "inventory", "status", "ok")));

var paymentFuture = ctx.runInChildContextAsync("check-payment", Map.class,
    child -> child.step("check", Map.class,
        stepCtx -> Map.of("service", "payment", "status", "ok")));

var shippingFuture = ctx.runInChildContextAsync("check-shipping", Map.class,
    child -> child.step("check", Map.class,
        stepCtx -> Map.of("service", "shipping", "status", "ok")));

// Wait for all to complete
DurableFuture.allOf(inventoryFuture, paymentFuture, shippingFuture);

var inventory = inventoryFuture.get();
var payment = paymentFuture.get();
var shipping = shippingFuture.get();

return Map.of(
    "inventory", inventory,
    "payment", payment,
    "shipping", shipping);
