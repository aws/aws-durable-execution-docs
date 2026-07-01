using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class AssertCallbackTest
{
    private static async Task<string> Workflow(object input, IDurableContext ctx)
    {
        return await ctx.WaitForCallbackAsync<string>(
            // In production this submitter hands the callback ID to an external system.
            async (callbackId, _, _) => await Task.CompletedTask,
            name: "approval");
    }

    [Fact]
    public async Task CompletesCallbackFromTest()
    {
        await using var runner = new DurableTestRunner<object, string>(Workflow);

        // Start drives the workflow to the point where the callback is waiting.
        string arn = await runner.StartAsync(new object());

        // Get the pending callback ID, then complete it from the test.
        string callbackId = await runner.WaitForCallbackAsync(arn, name: "approval");
        await runner.SendCallbackSuccessAsync(callbackId, "approved");

        // Drive the workflow the rest of the way to a terminal result.
        TestResult<string> result = await runner.WaitForResultAsync(arn);

        Assert.True(result.IsSucceeded);
        Assert.Equal("approved", result.Result);
    }
}
