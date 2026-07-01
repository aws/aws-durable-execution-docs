using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class LongWaitsTest
{
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        await ctx.WaitAsync(TimeSpan.FromHours(24), name: "cooling-off");
        return await ctx.StepAsync(async (_, _) => "done", name: "after-wait");
    }

    [Fact]
    public async Task CompletesWithLongWait()
    {
        // SkipTime defaults to true, so the day-long WaitAsync completes instantly.
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        TestResult<string> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);
        Assert.Equal("done", result.Result);

        var waitOps = result.Steps.Where(s => s.Kind == OperationKind.Wait).ToList();
        Assert.Single(waitOps);
        Assert.Equal("cooling-off", waitOps[0].Name);
    }
}
