using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class IncrementalBranchesExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<string>>(Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(object input, IDurableContext ctx)
    {
        await using var parallel = ctx.CreateParallel(name: "process");
        IParallelBranch<string> first = parallel.Branch("task-a", async (branch, ct) =>
            await branch.StepAsync(async (_, _) => "a done", name: "run-a"));
        IParallelBranch<string> second = parallel.Branch("task-b", async (branch, ct) =>
            await branch.StepAsync(async (_, _) => "b done", name: "run-b"));
        await parallel.CompleteAsync();
        return new[] { await first, await second };
    }
}
