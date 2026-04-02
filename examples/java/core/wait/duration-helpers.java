import java.time.Duration;

// Wait for 30 seconds
context.wait("wait30", Duration.ofSeconds(30));

// Wait for 5 minutes
context.wait("wait5m", Duration.ofMinutes(5));

// Wait for 2 hours
context.wait("wait2h", Duration.ofHours(2));

// Wait for 1 day
context.wait("wait1d", Duration.ofDays(1));
