// Wait strategy — decides, per poll, whether to continue and how long to wait
public interface IWaitStrategy<TState>
{
    WaitDecision Decide(TState state, int attemptNumber);
}

// Decision
public readonly record struct WaitDecision
{
    public bool ShouldContinue { get; }
    public TimeSpan Delay { get; }

    public static WaitDecision Stop();                        // condition met
    public static WaitDecision ContinueAfter(TimeSpan delay); // poll again after delay
}
