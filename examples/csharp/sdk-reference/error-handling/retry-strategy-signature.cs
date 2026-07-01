public interface IRetryStrategy
{
    // attempt is 1-based: 1 on the first retry, 2 on the second, etc.
    RetryDecision ShouldRetry(Exception exception, int attemptNumber);
}

// Build one from a delegate without implementing the interface:
public static IRetryStrategy RetryStrategy.FromDelegate(
    Func<Exception, int, RetryDecision> strategy);
