using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class TestRetriesTest
{
    [Fact]
    public async Task RetriesAndEventuallySucceeds()
    {
        int attempts = 0;

        Task<string> Workflow(object? input, IDurableContext ctx)
        {
            var config = new StepConfig
            {
                RetryStrategy = RetryStrategy.Exponential(
                    maxAttempts: 3,
                    initialDelay: TimeSpan.FromMilliseconds(1)),
            };

            return ctx.StepAsync(
                async (_, _) =>
                {
                    if (++attempts < 3)
                    {
                        throw new InvalidOperationException("transient error");
                    }
                    return "done";
                },
                name: "flaky",
                config: config);
        }

        await using var runner = new DurableTestRunner<object?, string>(
            Workflow,
            new TestRunnerOptions { SkipTime = true });

        TestResult<string> result = await runner.RunAsync(null);

        result.EnsureSucceeded();
        Assert.Equal("done", result.Result);
        Assert.Equal(3, result.GetStep("flaky").Attempt);
    }
}
