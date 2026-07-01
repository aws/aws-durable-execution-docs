// Returns a value
Task<T> RunInChildContextAsync<T>(
    Func<IDurableContext, CancellationToken, Task<T>> func,
    string? name = null,
    ChildContextConfig? config = null,
    CancellationToken cancellationToken = default);

// Returns no value
Task RunInChildContextAsync(
    Func<IDurableContext, CancellationToken, Task> func,
    string? name = null,
    ChildContextConfig? config = null,
    CancellationToken cancellationToken = default);
