Task<IBatchResult<TResult>> MapAsync<TItem, TResult>(
    IReadOnlyList<TItem> items,
    Func<IDurableContext, TItem, int, IReadOnlyList<TItem>, CancellationToken, Task<TResult>> func,
    string? name = null,
    MapConfig? config = null,
    CancellationToken cancellationToken = default);
