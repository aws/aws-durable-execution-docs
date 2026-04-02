// Short wait for rate limiting
context.wait(null, Duration.ofSeconds(30));

// Medium wait for polling intervals
context.wait(null, Duration.ofMinutes(5));

// Long wait for scheduled tasks
context.wait(null, Duration.ofHours(24));
