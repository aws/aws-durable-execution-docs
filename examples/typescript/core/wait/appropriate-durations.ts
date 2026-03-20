// Short wait for rate limiting
await context.wait({ seconds: 30 });

// Medium wait for polling intervals
await context.wait({ minutes: 5 });

// Long wait for scheduled tasks
await context.wait({ hours: 24 });
