using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class CloudRunnerTimeoutTest
{
    private static async Task<string> Workflow(GreetInput input, IDurableContext ctx)
    {
        return await ctx.StepAsync(
            async (_, _) => $"hello {input.Name}",
            name: "greet");
    }

    [Fact]
    public async Task RunsWithCustomTimeout()
    {
        // Set the default wall-clock timeout for polling via CloudTestRunnerOptions,
        // and tune how frequently the runner polls for completion.
        var options = new CloudTestRunnerOptions
        {
            InitialPollInterval = TimeSpan.FromMilliseconds(200),
            PollInterval = TimeSpan.FromSeconds(2),
            DefaultTimeout = TimeSpan.FromSeconds(60),
        };

        await using var runner = new CloudDurableTestRunner<GreetInput, string>(
            "arn:aws:lambda:us-east-1:123456789012:function:MyFunction:$LATEST",
            options: options);

        // Or override the timeout per call by passing it to RunAsync.
        TestResult<string> result = await runner.RunAsync(
            new GreetInput("world"),
            timeout: TimeSpan.FromSeconds(60));

        Assert.True(result.IsSucceeded);
    }
}

public record GreetInput(string Name);
