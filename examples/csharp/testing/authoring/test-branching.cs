using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class TestBranchingTest
{
    private static Task<string> Workflow(BranchInput input, IDurableContext ctx)
    {
        if (input.Premium)
        {
            return ctx.StepAsync(async (_, _) => "premium", name: "premium-path");
        }
        return ctx.StepAsync(async (_, _) => "standard", name: "standard-path");
    }

    private static DurableTestRunner<BranchInput, string> CreateRunner()
        => new(Workflow, new TestRunnerOptions { SkipTime = true });

    [Fact]
    public async Task TakesPremiumPath()
    {
        await using var runner = CreateRunner();

        TestResult<string> result = await runner.RunAsync(new BranchInput(Premium: true));

        result.EnsureSucceeded();
        Assert.Equal("premium", result.Result);
    }

    [Fact]
    public async Task TakesStandardPath()
    {
        await using var runner = CreateRunner();

        TestResult<string> result = await runner.RunAsync(new BranchInput(Premium: false));

        result.EnsureSucceeded();
        Assert.Equal("standard", result.Result);
    }
}

public record BranchInput(bool Premium);
