Task<T> WaitForCallbackAsync<T>(
    Func<string, IWaitForCallbackContext, CancellationToken, Task> submitter,
    string? name = null,
    WaitForCallbackConfig? config = null,
    CancellationToken cancellationToken = default);
