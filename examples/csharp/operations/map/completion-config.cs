using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class CompletionConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<ItemEvent, IReadOnlyList<string>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(
        ItemEvent input, IDurableContext ctx)
    {
        var config = new MapConfig
        {
            CompletionConfig = new CompletionConfig { MinSuccessful = 3 },
        };

        IBatchResult<string> result = await ctx.MapAsync(
            input.Items,
            async (itemCtx, item, index, items, ct) =>
                await itemCtx.StepAsync(
                    async (_, _) => item.ToUpperInvariant(),
                    name: $"process-{index}"),
            name: "process-items",
            config: config);

        return result.GetResults();
    }
}

public record ItemEvent(IReadOnlyList<string> Items);
