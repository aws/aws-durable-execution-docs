using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class BatchResultPointersExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<IReadOnlyList<Item>, IReadOnlyList<string>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(
        IReadOnlyList<Item> items, IDurableContext ctx)
    {
        IBatchResult<string> results = await ctx.MapAsync(
            items,
            async (itemCtx, item, index, allItems, ct) =>
                await itemCtx.StepAsync(
                    async (_, _) =>
                    {
                        Output output = ProcessItem(item);
                        return StoreOutput(output); // returns an S3 key, not the output itself
                    },
                    name: $"process-{index}"),
            name: "process-items");

        // `results` carries pointers, not payloads.
        return results.GetResults();
    }

    private static Output ProcessItem(Item item) => new Output(item.Id);
    private static string StoreOutput(Output output) => $"s3://outputs/{output.Id}";
}

public record Item(string Id);
public record Output(string Id);
