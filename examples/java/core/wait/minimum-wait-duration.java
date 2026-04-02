// Avoid - too short, will throw an exception
context.wait(null, Duration.ofSeconds(0));

// Minimum - 1 second
context.wait(null, Duration.ofSeconds(1));

// Better - use meaningful durations
context.wait(null, Duration.ofSeconds(5));
