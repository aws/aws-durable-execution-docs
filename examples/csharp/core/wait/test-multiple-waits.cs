using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class MultipleWaitsTests
{
    [Fact]
    public async Task HandlesMultipleSequentialWaits()
    {
        // SkipTime fast-forwards waits so the test runs instantly.
        await using var runner = new DurableTestRunner<object, MultipleWaitsResult>(
            handler: Workflow,
            options: new TestRunnerOptions { SkipTime = true });

        var result = await runner.RunAsync("test");
        result.EnsureSucceeded();

        Assert.Equal(2, result.Result!.CompletedWaits);
        Assert.Equal("done", result.Result!.FinalStep);

        // Both waits are recorded with their names.
        var waitOps = result.Steps.Where(s => s.Kind == OperationKind.Wait).ToList();
        Assert.Equal(2, waitOps.Count);

        var waitNames = waitOps.Select(w => w.Name).ToList();
        Assert.Contains("wait-1", waitNames);
        Assert.Contains("wait-2", waitNames);
    }

    private static async Task<MultipleWaitsResult> Workflow(object input, IDurableContext ctx)
    {
        await ctx.WaitAsync(TimeSpan.FromMinutes(5), name: "wait-1");
        await ctx.WaitAsync(TimeSpan.FromHours(1), name: "wait-2");
        return new MultipleWaitsResult(CompletedWaits: 2, FinalStep: "done");
    }
}

public record MultipleWaitsResult(int CompletedWaits, string FinalStep);
