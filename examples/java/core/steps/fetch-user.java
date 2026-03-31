public class FetchUserHandler extends DurableHandler<Map<String, String>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, DurableContext ctx) {
        var userId = event.get("user_id");

        // Step 1: Fetch user
        var user = ctx.step("fetch-user", Map.class, stepCtx -> {
            return Map.of("user_id", userId, "name", "Jane Doe", "email", "jane_doe@example.com");
        });

        // Step 2: Validate user
        var isValid = ctx.step("validate-user", Boolean.class, stepCtx -> {
            return user.containsKey("email");
        });

        if (!isValid) {
            return Map.of("status", "failed", "reason", "invalid_user");
        }

        // Step 3: Send notification
        var notification = ctx.step("send-notification", Map.class, stepCtx -> {
            return Map.of("sent", true, "email", user.get("email"));
        });

        return Map.of(
            "status", "completed",
            "user_id", userId,
            "notification_sent", notification.get("sent")
        );
    }
}
