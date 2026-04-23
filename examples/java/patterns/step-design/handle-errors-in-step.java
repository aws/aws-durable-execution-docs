import java.time.Duration;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.retry.JitterStrategy;
import software.amazon.lambda.durable.retry.RetryDecision;
import software.amazon.lambda.durable.retry.RetryStrategies;
import software.amazon.lambda.durable.retry.RetryStrategy;

static class TransientApiException extends RuntimeException {}
static class RateLimitException extends RuntimeException {}

// Retry only transient errors. Anything else fails immediately.
RetryStrategy retryStrategy = (error, attempt) -> {
    if (!(error instanceof TransientApiException) && !(error instanceof RateLimitException)) {
        return RetryDecision.fail();
    }
    return RetryStrategies.exponentialBackoff(
            5, Duration.ofSeconds(2), Duration.ofMinutes(1), 2.0, JitterStrategy.FULL)
        .makeRetryDecision(error, attempt);
};

context.step(
    "call-api",
    Record.class,
    ctx -> externalApi.get(input.id()),
    StepConfig.builder().retryStrategy(retryStrategy).build());
