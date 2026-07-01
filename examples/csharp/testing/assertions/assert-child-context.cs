using System.Linq;
using Amazon.Lambda.DurableExecution;
using Amazon.Lambda.DurableExecution.Testing;
using Xunit;

public class AssertChildContextTest
{
    private static Task<int> Workflow(object input, IDurableContext ctx)
        => ctx.RunInChildContextAsync(
            async (child, _) => await child.StepAsync(async (_, _) => 42, name: "compute"),
            name: "process");

    [Fact]
    public async Task AssertsOnChildContextAndItsOperations()
    {
        await using var runner = new DurableTestRunner<object, int>(Workflow);

        TestResult<int> result = await runner.RunAsync(new object());

        Assert.True(result.IsSucceeded);
        Assert.Equal(42, result.Result);

        // The child context is recorded as a CONTEXT-kind operation.
        TestStep context = result.GetStep("process");
        Assert.Equal(OperationKind.Context, context.Kind);
        Assert.Equal(OperationStatus.Succeeded, context.Status);

        // Walk the child operations to assert on what ran inside the context.
        TestStep compute = context.Children.Single(c => c.Name == "compute");
        Assert.Equal(OperationKind.Step, compute.Kind);
        Assert.Equal(42, compute.GetResult<int>());
    }
}
