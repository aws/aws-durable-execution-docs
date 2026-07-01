using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class SimpleMapExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<int>>(Workflow, input, context);

    private async Task<IReadOnlyList<int>> Workflow(object input, IDurableContext ctx)
    {
        IBatchResult<int> result = await ctx.MapAsync(
            new[] { 1, 2, 3, 4, 5 },
            async (itemCtx, item, index, items, ct) =>
                await itemCtx.StepAsync(
                    async (_, _) => item * item,
                    name: $"square-{index}"),
            name: "square-numbers");

        return result.GetResults();
    }
}
