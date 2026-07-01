// ctx.WaitForConditionAsync()
Task<TState> WaitForConditionAsync<TState>(
    Func<TState, IConditionCheckContext, CancellationToken, Task<TState>> check,
    WaitForConditionConfig<TState> config,
    string? name = null,
    CancellationToken cancellationToken = default);

// Check function
Func<TState, IConditionCheckContext, CancellationToken, Task<TState>>

// IConditionCheckContext — provides a logger and the 1-based attempt number
public interface IConditionCheckContext
{
    ILogger Logger { get; }
    int AttemptNumber { get; }
}

// Config (required; generic on the state type)
public sealed class WaitForConditionConfig<TState>
{
    public required TState InitialState { get; set; }
    public required IWaitStrategy<TState> WaitStrategy { get; set; }
}
