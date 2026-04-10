import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class ExponentialBackoffExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(
                3,
                Duration.ofSeconds(1),
                Duration.ofSeconds(10),
                2.0,
                JitterStrategy.FULL))
            .build();

        String result = context.step("retry_step", String.class,
            (StepContext ctx) -> "Step with exponential backoff",
            config);

        return "Result: " + result;
    }
}
