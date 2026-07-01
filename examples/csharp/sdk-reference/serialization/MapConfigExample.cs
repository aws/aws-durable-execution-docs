using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class MapConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<ProcessedItem>>(Workflow, input, context);

    private async Task<IReadOnlyList<ProcessedItem>> Workflow(object input, IDurableContext ctx)
    {
        // MapConfig has no serializer slot. Each item result is serialized with the
        // ILambdaSerializer registered on ILambdaContext.Serializer. To customize
        // serialization, register a custom ILambdaSerializer at the host boundary.
        var config = new MapConfig
        {
            MaxConcurrency = 3,
        };

        var items = new[] { "a", "b", "c" };
        IBatchResult<ProcessedItem> result = await ctx.MapAsync(
            items,
            async (itemCtx, item, index, all, ct) =>
                await itemCtx.StepAsync(
                    async (_, _) => new ProcessedItem(item, "done"),
                    name: $"process-{index}"),
            name: "process-items",
            config: config);
        return result.GetResults();
    }
}

public record ProcessedItem(string Id, string Status);
