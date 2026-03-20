// Wait for 30 seconds
await context.wait({ seconds: 30 });

// Wait for 5 minutes
await context.wait({ minutes: 5 });

// Wait for 2 hours
await context.wait({ hours: 2 });

// Wait for 1 day
await context.wait({ days: 1 });

// Combined - 1 hour and 30 minutes
await context.wait({ hours: 1, minutes: 30 });
