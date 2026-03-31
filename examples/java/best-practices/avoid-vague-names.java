// Generic names don't help with debugging
var user = ctx.step("step1", Map.class, stepCtx -> fetchUser(input.get("user_id")));
var validated = ctx.step("step2", Map.class, stepCtx -> validateUser(user));
var notification = ctx.step("step3", Map.class, stepCtx -> sendNotification(user));
return Map.of("status", "completed");
