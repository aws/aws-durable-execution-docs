using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.Serialization.SystemTextJson;

public class MapConfigExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<ProcessedItem>>(Workflow, input, context);

    private async Task<IReadOnlyList<ProcessedItem>> Workflow(object input, IDurableContext ctx)
    {
        // Set MapConfig.ItemSerializer to serialize each item's RESULT with a specific
        // ILambdaSerializer. When null (default), item results are serialized with the
        // ILambdaSerializer registered on ILambdaContext.Serializer. This controls only the
        // per-item result — not the aggregated batch envelope (statuses / completion
        // reason), which is SDK-internal. ParallelConfig has the same ItemSerializer field.
        var config = new MapConfig<string>
        {
            MaxConcurrency = 3,
            ItemSerializer = new CamelCaseLambdaJsonSerializer(),
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
