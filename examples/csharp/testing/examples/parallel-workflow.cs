using System.Collections.Generic;
using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class ParallelWorkflowTest
{
    private static async Task<IReadOnlyList<string>> Workflow(object input, IDurableContext ctx)
    {
        IBatchResult<string> results = await ctx.ParallelAsync(
            new[]
            {
                new DurableBranch<string>("fetch-a",
                    async (branch, _) => await branch.StepAsync(async (_, _) => "data-a", name: "fetch-a")),
                new DurableBranch<string>("fetch-b",
                    async (branch, _) => await branch.StepAsync(async (_, _) => "data-b", name: "fetch-b")),
                new DurableBranch<string>("fetch-c",
                    async (branch, _) => await branch.StepAsync(async (_, _) => "data-c", name: "fetch-c")),
            },
            name: "fetch-all");

        results.ThrowIfError();
        return results.GetResults();
    }

    [Fact]
    public async Task ExecutesBranchesInParallel()
    {
        await using var runner = new DurableTestRunner<object, IReadOnlyList<string>>(Workflow);

        TestResult<IReadOnlyList<string>> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);
        Assert.Equal(new[] { "data-a", "data-b", "data-c" }, result.Result!.ToArray());

        // Each parallel branch runs inside its own child context, recorded as a CONTEXT operation.
        var contextOps = result.Steps.Where(s => s.Kind == OperationKind.Context).ToList();
        Assert.NotEmpty(contextOps);
    }
}
