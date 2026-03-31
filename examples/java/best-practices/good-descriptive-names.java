var user = ctx.step("fetch_user", Map.class, stepCtx -> fetchUser(input.get("user_id")));
var validated = ctx.step("validate_user", Map.class, stepCtx -> validateUser(user));
var notification = ctx.step("send_notification", Map.class, stepCtx -> sendNotification(user));
return Map.of("status", "completed");
