// .NET has no config object; build strategies with the RetryStrategy factory.
public static IRetryStrategy RetryStrategy.Exponential(
    int maxAttempts = 3,
    TimeSpan? initialDelay = null,          // default 5s
    TimeSpan? maxDelay = null,              // default 300s
    double backoffRate = 2.0,
    JitterStrategy jitter = JitterStrategy.Full,
    Type[]? retryableExceptions = null,
    string[]? retryableMessagePatterns = null);
