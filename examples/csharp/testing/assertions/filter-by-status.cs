using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class FilterByStatusTest
{
    // A retried step reuses a single operation record, so the history holds one
    // entry per operation with its terminal status. Filter by status to separate
    // the step that succeeded from the one that failed.
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        await ctx.StepAsync(async (_, _) => "ok", name: "succeeds");
        await ctx.StepAsync<string>(
            async (_, _) => throw new InvalidOperationException("boom"),
            name: "fails",
            config: new StepConfig { RetryStrategy = RetryStrategy.None });
        return "done";
    }

    [Fact]
    public async Task FiltersOperationsByStatus()
    {
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        TestResult<string> result = await runner.RunAsync(new object());

        Assert.True(result.IsFailed);

        var succeeded = result.GetStepsByStatus(OperationStatus.Succeeded);
        Assert.Single(succeeded);
        Assert.Equal("succeeds", succeeded[0].Name);

        var failed = result.GetStepsByStatus(OperationStatus.Failed);
        Assert.Single(failed);
        Assert.Equal("fails", failed[0].Name);
    }
}
