// Returns a value
Task<T> StepAsync<T>(
    Func<IStepContext, CancellationToken, Task<T>> func,
    string? name = null,
    StepConfig? config = null,
    CancellationToken cancellationToken = default);

// Returns no value
Task StepAsync(
    Func<IStepContext, CancellationToken, Task> func,
    string? name = null,
    StepConfig? config = null,
    CancellationToken cancellationToken = default);
