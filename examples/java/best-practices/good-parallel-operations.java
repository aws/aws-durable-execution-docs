var userId = (String) input.get("user_id");
// Execute all three operations concurrently
DurableFuture<Map> userFuture = ctx.stepAsync("fetch_user_data", Map.class,
    stepCtx -> fetchUserData(userId));
DurableFuture<Map> ordersFuture = ctx.stepAsync("fetch_order_history", Map.class,
    stepCtx -> fetchOrderHistory(userId));
DurableFuture<Map> prefsFuture = ctx.stepAsync("fetch_preferences", Map.class,
    stepCtx -> fetchPreferences(userId));

return Map.of(
    "user", userFuture.get(),
    "orders", ordersFuture.get(),
    "preferences", prefsFuture.get()
);
