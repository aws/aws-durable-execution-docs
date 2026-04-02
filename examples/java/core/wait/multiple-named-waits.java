// Good - clear purpose
context.wait("rate_limit_cooldown", Duration.ofSeconds(60));
context.wait("polling_interval", Duration.ofMinutes(5));

// Less clear - unnamed waits
context.wait(null, Duration.ofSeconds(60));
context.wait(null, Duration.ofMinutes(5));
