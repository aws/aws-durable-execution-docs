using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class TestFailureTest
{
    private static async Task<string> Workflow(FailureInput input, IDurableContext ctx)
    {
        if (input.Fail)
        {
            throw new InvalidOperationException("intentional failure");
        }
        return await Task.FromResult("ok");
    }

    [Fact]
    public async Task ReportsFailedExecution()
    {
        await using var runner = new DurableTestRunner<FailureInput, string>(
            Workflow,
            new TestRunnerOptions { SkipTime = true });

        TestResult<string> result = await runner.RunAsync(new FailureInput(Fail: true));

        Assert.True(result.IsFailed);
        Assert.NotNull(result.Error);
    }
}

public record FailureInput(bool Fail);
