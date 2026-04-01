var userIds = List.of("user_1", "user_2", "user_3");

var result = ctx.map("process-users", userIds, Map.class,
    (userId, index, childCtx) -> {
        // Use steps within the map function
        var user = childCtx.step("fetch_user_" + index, Map.class,
            stepCtx -> Map.of("user_id", userId, "name", "Jane Doe", "email", "jane_doe@example.com"));

        var notification = childCtx.step("send_notification_" + index, Map.class,
            stepCtx -> Map.of("sent", true, "email", user.get("email")));

        return Map.of("user_id", userId, "notification_sent", notification.get("sent"));
    });

return Map.of("results", result.results());
