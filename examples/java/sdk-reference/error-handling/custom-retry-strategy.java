import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.RetryDecision;
import software.amazon.lambda.durable.retry.RetryStrategy;

public class CustomRetryStrategyExample extends DurableHandler<Map<String, Object>, String> {

    // RetryStrategy is a functional interface: (Throwable error, int attempt) -> RetryDecision
    // attempt is 1-based: 1 on the first retry, 2 on the second, etc.
    private static final RetryStrategy customRetryStrategy = (error, attempt) -> {
        if (attempt >= 4) {
            return RetryDecision.fail();
        }
        // Fixed 2-second delay regardless of attempt number
        return RetryDecision.retry(Duration.ofSeconds(2));
    };

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext context) {
        return context.step("call-api", String.class,
                stepCtx -> callApi(),
                StepConfig.builder().retryStrategy(customRetryStrategy).build());
    }

    private String callApi() {
        return "ok";
    }
}
