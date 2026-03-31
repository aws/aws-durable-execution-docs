var userId = (String) input.get("user_id");
// Sequential execution - each step waits for the previous one
var userData = ctx.step("fetch_user_data", Map.class, stepCtx -> fetchUserData(userId));
var orderHistory = ctx.step("fetch_order_history", Map.class, stepCtx -> fetchOrderHistory(userId));
var preferences = ctx.step("fetch_preferences", Map.class, stepCtx -> fetchPreferences(userId));

return Map.of(
    "user", userData,
    "orders", orderHistory,
    "preferences", preferences
);
