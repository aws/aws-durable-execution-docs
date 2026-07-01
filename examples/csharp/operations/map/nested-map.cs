using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NestedMapExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<RegionEvent, IReadOnlyList<IReadOnlyList<string>>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<IReadOnlyList<string>>> Workflow(
        RegionEvent input, IDurableContext ctx)
    {
        IBatchResult<IReadOnlyList<string>> result = await ctx.MapAsync(
            input.Regions,
            async (regionCtx, region, index, regions, ct) =>
            {
                IBatchResult<string> inner = await regionCtx.MapAsync(
                    region.Items,
                    async (itemCtx, item, i, items, innerCt) =>
                        await itemCtx.StepAsync(
                            async (_, _) => item.ToUpperInvariant(),
                            name: $"item-{i}"),
                    name: $"process-{region.Name}");
                return inner.GetResults();
            },
            name: "process-regions");

        return result.GetResults();
    }
}

public record Region(string Name, IReadOnlyList<string> Items);
public record RegionEvent(IReadOnlyList<Region> Regions);
