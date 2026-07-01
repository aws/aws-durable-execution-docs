using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class AssertWaitTest
{
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        await ctx.WaitAsync(TimeSpan.FromSeconds(30), name: "my-wait");
        return "done";
    }

    [Fact]
    public async Task AssertsOnWaitOperation()
    {
        // SkipTime is on by default, so the wait completes immediately and the
        // runner records a scheduled end timestamp for it.
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        TestResult<string> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);

        TestStep wait = result.GetStep("my-wait");
        Assert.Equal(OperationKind.Wait, wait.Kind);
        Assert.Equal(OperationStatus.Succeeded, wait.Status);
        Assert.NotNull(wait.GetWaitEndsAt());
    }
}
