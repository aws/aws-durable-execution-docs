using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class ErrorHandlingExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<ItemEvent, Summary>(Workflow, input, context);

    private async Task<Summary> Workflow(ItemEvent input, IDurableContext ctx)
    {
        IBatchResult<string> result = await ctx.MapAsync(
            input.Items,
            async (itemCtx, item, index, items, ct) =>
                await itemCtx.StepAsync(async (_, _) =>
                {
                    if (item == "bad") throw new InvalidOperationException("bad item");
                    return item.ToUpperInvariant();
                }, name: $"process-{index}"),
            name: "process-items");

        // BatchResult captures per-item failures rather than throwing. Inspect
        // HasFailure/GetErrors to handle them, or call ThrowIfError to propagate
        // the first failure.
        IReadOnlyList<DurableExecutionException> errors =
            result.HasFailure ? result.GetErrors() : Array.Empty<DurableExecutionException>();
        IReadOnlyList<string> successes = result.GetResults();

        return new Summary(result.SuccessCount, result.FailureCount, successes, errors);
    }
}

public record ItemEvent(IReadOnlyList<string> Items);
public record Summary(
    int Succeeded,
    int Failed,
    IReadOnlyList<string> Results,
    IReadOnlyList<DurableExecutionException> Errors);
