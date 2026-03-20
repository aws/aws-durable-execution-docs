// Avoid - too short, will raise an error
await context.wait({ seconds: 0 });

// Minimum - 1 second
await context.wait({ seconds: 1 });

// Better - use meaningful durations
await context.wait({ seconds: 5 });
