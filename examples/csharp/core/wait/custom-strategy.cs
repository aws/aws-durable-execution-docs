using Amazon.Lambda.DurableExecution;

public record JobState(string Status);

public static class CustomWaitStrategy
{
    // Full control over polling: the strategy decides whether to continue or
    // stop, and how long to wait before the next attempt.
    public static IWaitStrategy<JobState> Create() =>
        WaitStrategy.FromDelegate<JobState>((state, attempt) =>
        {
            if (state.Status == "COMPLETED")
            {
                return WaitDecision.Stop();
            }
            if (attempt >= 10)
            {
                throw new WaitForConditionException("Max attempts exceeded");
            }
            return WaitDecision.ContinueAfter(TimeSpan.FromSeconds(attempt * 5));
        });
}
