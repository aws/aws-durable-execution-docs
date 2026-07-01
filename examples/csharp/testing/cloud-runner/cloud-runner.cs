using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class CloudRunnerTest
{
    // The workflow under test. In cloud mode it runs in the deployed function;
    // it is shown here so the example is self-contained.
    private static async Task<string> Workflow(GreetInput input, IDurableContext ctx)
    {
        return await ctx.StepAsync(
            async (_, _) => $"hello {input.Name}",
            name: "greet");
    }

    [Fact]
    public async Task RunsAgainstDeployedFunction()
    {
        await using var runner = new CloudDurableTestRunner<GreetInput, string>(
            "arn:aws:lambda:us-east-1:123456789012:function:MyFunction:$LATEST");

        TestResult<string> result = await runner.RunAsync(new GreetInput("world"));

        Assert.True(result.IsSucceeded);
        Assert.Equal("hello world", result.Result);
    }
}

public record GreetInput(string Name);
