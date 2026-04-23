// Wait 24 hours with no active Lambda time.
await context.wait("cool-off", { hours: 24 });
