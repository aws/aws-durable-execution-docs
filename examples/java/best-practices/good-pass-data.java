var user = ctx.step("fetch_user", Map.class, stepCtx -> {
    return Map.of("user_id", input.get("user_id"), "name", "Jane Doe");
});

var sent = ctx.step("send_email", Boolean.class, stepCtx -> {
    sendToAddress(user.get("name"), user.get("email"));
    return true;
});
return Map.of("sent", sent);
