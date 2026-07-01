using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NestedParallelExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<IReadOnlyList<string>>>(
            Workflow, input, context);

    private async Task<IReadOnlyList<IReadOnlyList<string>>> Workflow(
        object input, IDurableContext ctx)
    {
        IBatchResult<IReadOnlyList<string>> outer = await ctx.ParallelAsync(
            new Func<IDurableContext, CancellationToken, Task<IReadOnlyList<string>>>[]
            {
                async (branch, ct) =>
                {
                    // A branch can call ParallelAsync to nest parallel operations.
                    IBatchResult<string> inner = await branch.ParallelAsync(
                        new Func<IDurableContext, CancellationToken, Task<string>>[]
                        {
                            async (c, t) => await c.StepAsync(async (_, _) => "a1", name: "a1"),
                            async (c, t) => await c.StepAsync(async (_, _) => "a2", name: "a2"),
                        },
                        name: "inner-a");
                    return inner.GetResults();
                },
                async (branch, ct) =>
                {
                    IBatchResult<string> inner = await branch.ParallelAsync(
                        new Func<IDurableContext, CancellationToken, Task<string>>[]
                        {
                            async (c, t) => await c.StepAsync(async (_, _) => "b1", name: "b1"),
                            async (c, t) => await c.StepAsync(async (_, _) => "b2", name: "b2"),
                        },
                        name: "inner-b");
                    return inner.GetResults();
                },
            },
            name: "outer");

        return outer.GetResults();
    }
}
