// Good - log identifiers only
ctx.getLogger().info("User authenticated, user_id={}", userId);

// Avoid - don't log sensitive data
ctx.getLogger().info("User authenticated, password={}", password);
