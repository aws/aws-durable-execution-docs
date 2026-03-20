// Good - clear purpose
await context.wait("rate_limit_cooldown", { seconds: 60 });
await context.wait("polling_interval", { minutes: 5 });

// Less clear - unnamed waits
await context.wait({ seconds: 60 });
await context.wait({ minutes: 5 });
