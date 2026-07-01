using Amazon.Lambda.DurableExecution;

public record JobState(string Status);

public static class StrategyHelper
{
    // Build an exponential-backoff strategy from common parameters. The
    // isDone predicate stops polling once the latest state satisfies it.
    public static IWaitStrategy<JobState> Create() =>
        WaitStrategy.Exponential<JobState>(
            maxAttempts: 10,
            initialDelay: TimeSpan.FromSeconds(5),
            maxDelay: TimeSpan.FromMinutes(5),
            backoffRate: 2.0,
            jitter: JitterStrategy.Full,
            isDone: state => state.Status == "COMPLETED");
}
