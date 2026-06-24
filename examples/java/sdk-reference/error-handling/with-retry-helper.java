import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.WithRetryConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class WithRetryHelperExample extends DurableHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext context) {
        WithRetryConfig retryConfig = WithRetryConfig.builder()
                .retryStrategy(RetryStrategies.exponentialBackoff(
                        3,                       // maxAttempts
                        Duration.ofSeconds(2),   // initialDelay
                        Duration.ofMinutes(1),   // maxDelay
                        2.0,                     // backoffRate
                        JitterStrategy.FULL))    // jitter
                .build();

        // invoke does not accept a retry strategy, so withRetry applies backoff
        // between failed attempts.
        return context.withRetry(
                "charge-payment",
                (attempt, ctx) -> ctx.invoke(
                        "charge-" + attempt,
                        "process-payment",
                        Map.of("orderId", event.get("orderId")),
                        String.class),
                retryConfig);
    }
}
