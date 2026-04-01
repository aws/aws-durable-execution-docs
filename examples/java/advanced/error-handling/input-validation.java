public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext ctx) {
    // Validate required fields
    if (event.get("user_id") == null || ((String) event.get("user_id")).isEmpty()) {
        return Map.of("error", "InvalidInput", "message", "user_id is required");
    }

    if (event.get("action") == null || ((String) event.get("action")).isEmpty()) {
        return Map.of("error", "InvalidInput", "message", "action is required");
    }

    // Process valid input
    var userId = (String) event.get("user_id");
    var action = (String) event.get("action");

    var result = ctx.step("process_action", Map.class,
        stepCtx -> Map.of("user_id", userId, "action", action, "status", "completed"));

    return result;
}
