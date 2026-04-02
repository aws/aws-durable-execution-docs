import java.time.Duration;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.WaitStrategies;

var strategy = WaitStrategies.<Map<String, String>>exponentialBackoff(
        10,                          // maxAttempts
        Duration.ofSeconds(5),       // initialDelay
        Duration.ofMinutes(5),       // maxDelay
        2.0,                         // backoffRate
        JitterStrategy.FULL);        // jitter
