import java.time.Duration;
import software.amazon.lambda.durable.retry.WaitStrategies;
import software.amazon.lambda.durable.retry.JitterStrategy;

var strategy = WaitStrategies.<Map<String, String>>builder(
        state -> !"COMPLETED".equals(state.get("status")))
    .maxAttempts(10)
    .initialDelay(Duration.ofSeconds(5))
    .maxDelay(Duration.ofMinutes(5))
    .backoffRate(2.0)
    .jitter(JitterStrategy.FULL)
    .build();
