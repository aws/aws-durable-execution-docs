import java.time.Duration;
import java.util.Map;
import software.amazon.lambda.durable.DurableContext;
import software.amazon.lambda.durable.DurableHandler;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryDecision;
import software.amazon.lambda.durable.retry.RetryStrategies;
import software.amazon.lambda.durable.retry.RetryStrategy;

public class RetrySpecificErrorsExample extends DurableHandler<Map<String, Object>, String> {

    static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) { super(message); }
    }

    static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) { super(message); }
    }

    // RetryStrategy is a functional interface: (Throwable error, int attempt) -> RetryDecision
    // Filter by error type manually, then delegate to exponential backoff for the delay.
    private static final RetryStrategy retryStrategy = (error, attempt) -> {
        if (!(error instanceof RateLimitException) && !(error instanceof ServiceUnavailableException)) {
            return RetryDecision.fail(); // all other errors fail immediately
        }
        return RetryStrategies.exponentialBackoff(5, Duration.ofSeconds(2), Duration.ofMinutes(1), 2.0, JitterStrategy.FULL)
                .makeRetryDecision(error, attempt);
    };

    @Override
    public String handleRequest(Map<String, Object> event, DurableContext context) {
        return context.step("call-api", String.class,
                stepCtx -> callApi(),
                StepConfig.builder().retryStrategy(retryStrategy).build());
    }

    private String callApi() { return "ok"; }
}
