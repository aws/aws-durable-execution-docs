Task<TResult> InvokeAsync<TPayload, TResult>(
    string functionName,
    TPayload payload,
    string? name = null,
    InvokeConfig? config = null,
    CancellationToken cancellationToken = default);
