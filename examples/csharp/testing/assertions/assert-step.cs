using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class AssertStepTest
{
    // Look up a step by name, then check its kind, status, and typed result.
    private static Task<int> Workflow(object input, IDurableContext ctx)
        => ctx.StepAsync(async (_, _) => 42, name: "compute");

    [Fact]
    public async Task AssertsOnStepOperation()
    {
        await using var runner = new DurableTestRunner<object, int>(Workflow);

        TestResult<int> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);

        TestStep step = result.GetStep("compute");
        Assert.Equal(OperationKind.Step, step.Kind);
        Assert.Equal(OperationStatus.Succeeded, step.Status);
        Assert.Equal(42, step.GetResult<int>());
    }
}
