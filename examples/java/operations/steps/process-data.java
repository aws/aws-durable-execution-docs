import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.StepContext;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.config.StepSemantics;
import software.amazon.lambda.durable.retry.RetryStrategies;

public class ProcessDataExample extends DurableHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, DurableContext context) {
        StepConfig config = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(
                3,
                java.time.Duration.ofSeconds(1),
                java.time.Duration.ofMinutes(5),
                2.0,
                software.amazon.lambda.durable.retry.JitterStrategy.FULL))
            .semantics(StepSemantics.AT_LEAST_ONCE_PER_RETRY)
            .build();

        Map<String, Object> result = context.step("process_data", Map.class,
            (StepContext ctx) -> Map.of("processed", event.get("data"), "status", "completed"),
            config);

        return result;
    }
}
