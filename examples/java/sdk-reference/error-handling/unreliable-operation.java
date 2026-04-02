import java.time.Duration;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class UnreliableOperationExample extends DurableHandler<Object, String> {
    @Override
    public String handleRequest(Object input, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(
                3,
                Duration.ofSeconds(1),
                Duration.ofMinutes(5),
                2.0,
                JitterStrategy.FULL))
            .build();

        String result = context.step("unreliable_operation", String.class,
            (StepContext ctx) -> {
                if (Math.random() > 0.5) throw new RuntimeException("Random error occurred");
                return "Operation succeeded";
            },
            config);

        return result;
    }
}
