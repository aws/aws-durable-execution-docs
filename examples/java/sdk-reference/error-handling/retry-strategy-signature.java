import software.amazon.lambda.durable.retry.RetryDecision;
import software.amazon.lambda.durable.retry.RetryStrategy;

// @FunctionalInterface
// interface RetryStrategy {
//     RetryDecision makeRetryDecision(Throwable error, int attempt);
// }
// attempt is one-indexed: 1 on the first retry, 2 on the second, etc.
