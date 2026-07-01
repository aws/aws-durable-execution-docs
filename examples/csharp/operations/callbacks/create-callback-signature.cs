Task<ICallback<T>> CreateCallbackAsync<T>(
    string? name = null,
    CallbackConfig? config = null,
    CancellationToken cancellationToken = default);
