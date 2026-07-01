using Amazon.Lambda.Core;
using Amazon.Lambda.DurableExecution;

public class PassArgumentsExample
{
    public Task<DurableExecutionInvocationOutput> Handler(
        DurableExecutionInvocationInput input, ILambdaContext context)
        => DurableFunction.WrapAsync<object, IReadOnlyList<string>>(Workflow, input, context);

    private async Task<IReadOnlyList<string>> Workflow(object input, IDurableContext ctx)
    {
        var items = new[] { "a", "b", "c" };

        // Capture each item in the closure. Copy the loop variable to a local
        // so every branch captures its own value.
        var branches = new List<DurableBranch<string>>();
        foreach (var item in items)
        {
            var captured = item;
            branches.Add(new DurableBranch<string>($"process-{captured}",
                async (branch, ct) => await branch.StepAsync(
                    async (_, _) => $"processed {captured}", name: $"run-{captured}")));
        }

        IBatchResult<string> result = await ctx.ParallelAsync(branches, name: "process-items");

        return result.GetResults();
    }
}
