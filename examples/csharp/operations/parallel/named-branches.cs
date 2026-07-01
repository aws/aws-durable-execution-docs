using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class NamedBranchesExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<string>>(Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(object input, IDurableContext ctx)
    {
        // Each DurableBranch carries a name surfaced on IBatchItem.Name.
        IBatchResult<string> result = await ctx.ParallelAsync(
            new[]
            {
                new DurableBranch<string>("task-a", async (branch, ct) =>
                    await branch.StepAsync(async (_, _) => "a done", name: "run-a")),
                new DurableBranch<string>("task-b", async (branch, ct) =>
                    await branch.StepAsync(async (_, _) => "b done", name: "run-b")),
            },
            name: "process");

        return result.GetResults();
    }
}
