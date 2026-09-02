import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.WaitStrategies;

var defaultStrategy = WaitStrategies.<Map<String, String>>defaultStrategy();

var exponentialStrategy = WaitStrategies.<Map<String, String>>exponentialBackoff(
        10,                          // maxAttempts
        Duration.ofSeconds(5),       // initialDelay
        Duration.ofMinutes(5),       // maxDelay
        2.0,                         // backoffRate
        JitterStrategy.FULL);        // jitter

var fixedDelayStrategy = WaitStrategies.<Map<String, String>>fixedDelay(
        10,                          // maxAttempts
        Duration.ofSeconds(30));     // fixedDelay
