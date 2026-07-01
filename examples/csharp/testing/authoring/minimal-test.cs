using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class MinimalTest
{
    private static Task<string> Workflow(object? input, IDurableContext ctx)
        => ctx.StepAsync(async (_, _) => "hello", name: "greet");

    [Fact]
    public async Task ReturnsExpectedResult()
    {
        await using var runner = new DurableTestRunner<object?, string>(
            Workflow,
            new TestRunnerOptions { SkipTime = true });

        TestResult<string> result = await runner.RunAsync(null);

        result.EnsureSucceeded();
        Assert.Equal("hello", result.Result);
    }
}
