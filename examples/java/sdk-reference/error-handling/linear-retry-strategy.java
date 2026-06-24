import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class LinearRetryStrategyExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .retryStrategy(RetryStrategies.linearBackoff(
                5,                       // maxAttempts
                Duration.ofSeconds(2),   // initialDelay
                Duration.ofSeconds(30),  // maxDelay
                Duration.ofSeconds(3),   // increment
                JitterStrategy.FULL))    // jitter
            .build();

        return context.step("call-external-api", String.class,
            (StepContext ctx) -> "ok",
            config);
    }
}
