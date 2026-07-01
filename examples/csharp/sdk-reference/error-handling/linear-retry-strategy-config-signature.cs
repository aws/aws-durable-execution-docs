// .NET ships no linear strategy. Build one with RetryStrategy.FromDelegate and
// compute a constant or linearly growing delay yourself.
public static IRetryStrategy RetryStrategy.FromDelegate(
    Func<Exception, int, RetryDecision> strategy);

// The delegate returns a RetryDecision:
public static RetryDecision RetryDecision.RetryAfter(TimeSpan delay);
public static RetryDecision RetryDecision.DoNotRetry();
